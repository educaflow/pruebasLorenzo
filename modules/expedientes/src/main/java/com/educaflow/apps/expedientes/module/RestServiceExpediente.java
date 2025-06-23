package com.educaflow.apps.expedientes.module;
import com.axelor.rpc.Resource;
import com.axelor.web.service.ResourceService;
import com.axelor.web.service.RestService;
import com.educaflow.apps.expedientes.db.Expediente;
import com.google.inject.Key;
import com.google.inject.util.Types;
import java.lang.reflect.Type;

public class RestServiceExpediente extends RestService {

    @Override
    protected Resource<?> getResource() {

        Type type;
        if (Expediente.class.isAssignableFrom(entityClass())) {
            type = Types.newParameterizedType(ResourceExpediente.class, entityClass());
        } else {
            type = Types.newParameterizedType(Resource.class, entityClass());
        }


        return (Resource<?>) getInjector().getInstance(Key.get(type));
    }

}