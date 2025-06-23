package com.educaflow.apps.expedientes.module;

import com.axelor.db.JpaSecurity;
import com.axelor.db.Repository;
import com.axelor.event.Event;
import com.axelor.events.PostRequest;
import com.axelor.events.PreRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Request;
import com.axelor.rpc.Resource;
import com.axelor.rpc.Response;
import com.educaflow.apps.expedientes.common.ExpedienteController;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.common.util.AxelorDBUtil;
import com.google.inject.TypeLiteral;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;


public class ResourceExpediente<T extends Expediente> extends Resource<T> {

    @Inject
    ExpedienteController expedienteController;

    @Inject
    @SuppressWarnings("unchecked")
    public ResourceExpediente(
            TypeLiteral<T> typeLiteral,
            Provider<JpaSecurity> security,
            Event<PreRequest> preRequest,
            Event<PostRequest> postRequest) {
        super(typeLiteral, security, preRequest, postRequest);
    }

    @Override
    public Response save(final Request request) {
        ActionResponse response = new ActionResponse();

        Expediente expediente=expedienteController.triggerEvent(request,response);

        Repository respository= AxelorDBUtil.getRepository(expediente.getClass());
        List<Object> data=new ArrayList<>();
        data.add(respository.populate(toMap(expediente, request), request.getContext()));

        response.setData(data);
        response.setStatus(Response.STATUS_SUCCESS);
        return response;
    }

}
