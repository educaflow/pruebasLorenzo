#!/bin/bash

# --- Variables de Configuración del Certificado ---
# Si necesitas un campo vacío, déjalo como una cadena vacía (ej. ORGANIZATION_UNIT="")

# Datos de la Autoridad de Certificación (CA)
CA_COUNTRY="ES"
CA_STATE="Valencia"
CA_LOCALITY="Valencia"
CA_ORGANIZATION="Mi Propia CA"
CA_ORGANIZATION_UNIT="Departamento IT" # Opcional
CA_COMMON_NAME="Mi CA Raiz"
CA_EMAIL="ca@example.com"
CA_DAYS=3650 # Días de validez del certificado CA (10 años)

# Datos de tu Certificado Personal
USER_COUNTRY="ES"
USER_STATE="Valencia"
USER_LOCALITY="Valencia"
USER_ORGANIZATION="Mi Empresa"
USER_ORGANIZATION_UNIT="Desarrollo" # Opcional
USER_COMMON_NAME="Mariano Monzo 12345678Z"
USER_EMAIL="usuario@example.com"
USER_DAYS=730 # Días de validez del certificado personal (2 años)

# Contraseña para la clave privada del certificado personal (para el archivo P12)
P12_PASSWORD="nada" # ¡CAMBIA ESTO POR UNA CONTRASEÑA SEGURA!

# Nombres de los archivos de salida
CA_KEY="ca_private.key"
CA_CERT="ca_certificate.crt"
USER_KEY="user_private.key"
USER_CSR="user_request.csr"
USER_CERT="user_certificate.crt"
USER_P12="my_certificate.p12"

echo "Iniciando la generación de certificados..."
echo "-----------------------------------------"

# 1. Generar la clave privada de la CA
echo "Generando clave privada para la CA ($CA_KEY)..."
openssl genrsa -out "$CA_KEY" 2048

# 2. Generar el certificado de la CA (autofirmado)
echo "Generando certificado autofirmado para la CA ($CA_CERT)..."
openssl req -x509 -new -nodes -key "$CA_KEY" -sha256 -days "$CA_DAYS" -out "$CA_CERT" \
    -subj "/C=$CA_COUNTRY/ST=$CA_STATE/L=$CA_LOCALITY/O=$CA_ORGANIZATION/OU=$CA_ORGANIZATION_UNIT/CN=$CA_COMMON_NAME/emailAddress=$CA_EMAIL"

echo "CA generada exitosamente:"
echo "  - Clave privada de CA: $CA_KEY"
echo "  - Certificado de CA: $CA_CERT"
echo "-----------------------------------------"

# 3. Generar la clave privada para tu certificado personal
echo "Generando clave privada para tu certificado personal ($USER_KEY)..."
openssl genrsa -out "$USER_KEY" 2048

# 4. Generar la solicitud de firma de certificado (CSR) para tu certificado personal
echo "Generando solicitud de firma de certificado (CSR) para tu certificado personal ($USER_CSR)..."
openssl req -new -key "$USER_KEY" -out "$USER_CSR" \
    -subj "/C=$USER_COUNTRY/ST=$USER_STATE/L=$USER_LOCALITY/O=$USER_ORGANIZATION/OU=$USER_ORGANIZATION_UNIT/CN=$USER_COMMON_NAME/emailAddress=$USER_EMAIL"

# 5. Firmar tu certificado personal con la CA
echo "Firmando tu certificado personal con la CA ($USER_CERT)..."
openssl x509 -req -in "$USER_CSR" -CA "$CA_CERT" -CAkey "$CA_KEY" -CAcreateserial -out "$USER_CERT" -days "$USER_DAYS" -sha256

echo "Certificado personal generado y firmado exitosamente:"
echo "  - Clave privada personal: $USER_KEY"
echo "  - Solicitud de firma personal (CSR): $USER_CSR"
echo "  - Certificado personal firmado: $USER_CERT"
echo "-----------------------------------------"

# 6. Crear el archivo P12 que incluye la clave privada y el certificado
echo "Creando el archivo P12 ($USER_P12)..."
openssl pkcs12 -export -out "$USER_P12" -inkey "$USER_KEY" -in "$USER_CERT" -name "$USER_COMMON_NAME" -passout pass:"$P12_PASSWORD"

echo "¡Script completado!"
echo "-----------------------------------------"
echo "Archivos generados:"
echo "  - Certificado de la Autoridad de Certificación: $CA_CERT"
echo "  - Tu certificado personal (incluye clave privada): $USER_P12"
echo ""
echo "Recuerda que la contraseña para el archivo P12 es: '$P12_PASSWORD'"
echo "Guarda tus claves privadas de forma segura."