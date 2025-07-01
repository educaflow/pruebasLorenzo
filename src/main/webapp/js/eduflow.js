const { protocol, hostname, port, pathname } = window.location;
const baseUrl = `${protocol}//${hostname}${port ? `:${port}` : ''}${pathname.replace(/\/[^/]*$/, '')}`;

async function signDocument(context, sourceKey, targetKey, modelName, sufijo,tipoDocumento) {
    try {
        // 1. Actualizar contexto antes de firmar
        const updatedContext = await refreshContext(modelName, context.id);

        const source = updatedContext[sourceKey];
        const idSource = source.id;
        const versionSource = source.$version;
        const originalFileName = source.fileName;

        // 2. Crear expedienteContext local para no pisar variables globales
        const expedienteContext = {
            id: updatedContext.id,
            version: updatedContext.version,
            model: modelName,
            sufijo,
            source: {
                id: idSource,
                version: versionSource,
                fileName: originalFileName,
                field: sourceKey
            },
            target: {
                field: targetKey
            }
        };

        console.log("Contexto del expediente:", expedienteContext);

        // 3. Descargar PDF base64
        const urlPdf = `${baseUrl}/ws/rest/com.axelor.meta.db.MetaFile/${idSource}/content/download?version=${versionSource}`;
        const base64Pdf = await fetchPdfBase64(urlPdf);
        console.log('PDF base64 descargado');

        // 4. Firmar PDF
        const firmaB64 = await firmarBase64(base64Pdf,tipoDocumento);

        // 5. Subir PDF firmado
        await subirAFCTComoMetaFile(firmaB64, expedienteContext);

        console.log("Documento firmado procesado correctamente");

    } catch (error) {
        console.error("Error en signDocument:", error);
        alert("Error al firmar el documento: " + error.message);
    }
}

async function refreshContext(modelName, id) {
    const url = `${baseUrl}/ws/rest/${modelName}/${id}`;
    console.log(`Obteniendo contexto actualizado de ${modelName} con ID ${id} desde: ${url}`);
    const csrfToken = getCookie('CSRF-TOKEN');
    const response = await fetch(url, {
        method: 'GET',
        headers: { "Accept": "application/json", "X-CSRF-TOKEN": csrfToken },
        credentials: 'include'
    });
    if (!response.ok) throw new Error('Error al obtener la entidad actualizada');
    const json = await response.json();
    return json.data[0];
}

async function fetchPdfBase64(url) {
    const response = await fetch(url);
    if (!response.ok) throw new Error('Error al descargar PDF');
    const blob = await response.blob();

    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => {
            const base64data = reader.result.split(',')[1];
            resolve(base64data);
        };
        reader.onerror = reject;
        reader.readAsDataURL(blob);
    });
}

// Convertir firmarBase64 a async que devuelve promesa
function firmarBase64(base64Pdf,tipoDocumento) {
    return new Promise((resolve, reject) => {
        AutoScript.cargarAppAfirma();
        const numeroDNI = "12345678Z"; // Actualiza según necesites

        const servletsBase = window.location.href.includes("/firmaMovil/")
            ? window.location.href.substring(0, window.location.href.indexOf("/firmaMovil/") + "/firmaMovil/".length - 1)
            : window.location.origin;

        let params = "mode=explicit\n" +
            "serverUrl=" + servletsBase + "/afirma-server-triphase-signer/SignatureService";



        if (tipoDocumento===1) {
            params += "\n" +
                "signaturePositionOnPageLowerLeftX=300\n" +
                "signaturePositionOnPageLowerLeftY=50\n" +
                "signaturePositionOnPageUpperRightX=600\n" +
                "signaturePositionOnPageUpperRightY=150\n" +
                "signaturePage=1";

        } else {
            params += "\n" +
                "signaturePositionOnPageLowerLeftX=40\n" +
                "signaturePositionOnPageLowerLeftY=50\n" +
                "signaturePositionOnPageUpperRightX=290\n" +
                "signaturePositionOnPageUpperRightY=150\n" +
                "signaturePage=1";
        }

        var huella="F9:C4:78:01:59:65:C1:C9:27:9F:9E:81:8E:E5:03:19:C2:5C:7D:C0"

        //params += "\nheadless=true\n;nonexpired:\nfilters=thumbprint:SHA1:" + huella;

        AutoScript.sign(
            base64Pdf,
            "SHA256withRSA",
            "PAdES",
            params,
            (signatureB64, certB64, extraData) => {
                console.log("Firma OK");
                resolve(signatureB64);
            },
            (errorType, errorMessage) => {
                console.error("Error al firmar:", errorType, errorMessage);
                reject(new Error(errorMessage));
            }
        );
    });
}

async function subirAFCTComoMetaFile(base64Firmado, expedienteContext) {
    const blob = base64ToBlob(base64Firmado, "application/pdf");
    const fileName = `${expedienteContext.source.fileName}${expedienteContext.sufijo}.pdf`;
    const fileType = "application/pdf";
    const fileSize = blob.size;

    const formData = new FormData();
    formData.append("file", blob, fileName);
    formData.append("field", expedienteContext.target.field);

    const requestPayload = {
        data: {
            fileName,
            fileType,
            fileSize,
            "$upload": { file: {} }
        }
    };
    formData.append("request", JSON.stringify(requestPayload));

    const csrfToken = getCookie('CSRF-TOKEN');
    if (!csrfToken) throw new Error("CSRF-TOKEN no encontrado");

    const response = await fetch(`${baseUrl}/ws/rest/com.axelor.meta.db.MetaFile/upload`, {
        method: "POST",
        body: formData,
        credentials: "include",
        headers: { "X-CSRF-TOKEN": csrfToken }
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(`Error subiendo a MetaFile: ${response.status} ${response.statusText} - ${text}`);
    }

    const metaFileData = await response.json();

    const metaFileId = metaFileData?.data?.[0]?.id;
    if (!metaFileId) throw new Error("ID de MetaFile no obtenido");

    return actualizarFCTConMetaFile(metaFileId, expedienteContext);
}

async function actualizarFCTConMetaFile(metaFileId, expedienteContext) {
    const url = `${baseUrl}/ws/rest/${expedienteContext.model}/${expedienteContext.id}`;
    const firmadoField = expedienteContext.target.field;

    const payload = {
        data: {
            id: expedienteContext.id,
            version: expedienteContext.version,
            [firmadoField]: {
                id: metaFileId,
                version: 0
            }
        }
    };

    const csrfToken = getCookie('CSRF-TOKEN');
    if (!csrfToken) throw new Error("CSRF-TOKEN no encontrado");

    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify(payload),
        credentials: "include"
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(`Error actualizando el FCT: ${response.status} ${response.statusText} - ${text}`);
    }

    const data = await response.json();
    console.log("FCT actualizado con documento firmado:", data);

    if (data?.data?.version !== undefined) {
        expedienteContext.version = data.data.version;
        console.log("Nueva versión guardada:", expedienteContext.version);
    }

    alert("Documento firmado guardado correctamente en la ficha");
}

// Utilidades
function base64ToBlob(base64, mime) {
    const byteChars = atob(base64);
    const byteNumbers = new Array(byteChars.length);
    for (let i = 0; i < byteChars.length; i++) {
        byteNumbers[i] = byteChars.charCodeAt(i);
    }
    return new Blob([new Uint8Array(byteNumbers)], { type: mime });
}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}
