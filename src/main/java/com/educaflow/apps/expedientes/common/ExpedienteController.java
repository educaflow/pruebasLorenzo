package com.educaflow.apps.expedientes.common;

import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.educaflow.apps.expedientes.common.annotations.BeanValidationRulesForStateAndEvent;
import com.educaflow.apps.expedientes.db.Expediente;
import com.educaflow.apps.expedientes.db.ExpedienteHistorialEstados;
import com.educaflow.apps.expedientes.db.Profile;
import com.educaflow.apps.expedientes.db.TipoExpediente;
import com.educaflow.apps.expedientes.db.repo.ProfileRepository;
import com.educaflow.apps.expedientes.db.repo.TipoExpedienteRepository;
import com.educaflow.common.mapper.BeanMapperModel;
import com.educaflow.common.util.AxelorDBUtil;
import com.educaflow.common.util.AxelorViewUtil;
import com.educaflow.common.util.ReflectionUtil;
import com.educaflow.common.util.TextUtil;
import com.educaflow.common.validation.engine.*;
import com.educaflow.common.validation.messages.BusinessMessages;
import com.google.common.base.CaseFormat;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;


public class ExpedienteController {

    @Inject
    TipoExpedienteRepository tipoExpedienteRepository;


    public ExpedienteController() {;

    }

    @CallMethod
    @Transactional
    public void triggerInitialEvent(ActionRequest request, ActionResponse response) {
        try {
            TipoExpediente tipoExpediente = getTipoExpediente(request);
            EventManager eventManager=getEventManager(tipoExpediente);
            EventContext eventContext = getEventContext(eventManager,request);
            JpaRepository<Expediente> expedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());
            Enum initialEvent=getInitialState(eventManager.getStateClass());

            Expediente expediente=(Expediente)eventManager.getModelClass().getDeclaredConstructor().newInstance();
            expediente.setTipoExpediente(tipoExpediente);
            updateName(expediente);

            eventManager.triggerInitialEvent(expediente, eventContext);
            expediente.updateState(initialEvent);
            addHistorialEstado(expediente,null);
            eventManager.onEnterState(expediente, eventContext);
            saveExpediente(expedienteRepository,expediente);

            String viewName = eventManager.getViewName(expediente, eventContext);
            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,getTabName(expediente),eventContext.getProfile().name());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @CallMethod
    @Transactional
    public void triggerEvent(ActionRequest request, ActionResponse response) {
        try {
            String eventName=getEventName(request);
            Expediente expediente=getExpedienteFromDB(request);
            Expediente expedienteOriginal=(Expediente) BeanMapperModel.getEntityCloned(expediente.getClass(), expediente);
            EventManager eventManager=getEventManager(expediente.getTipoExpediente());
            EventContext eventContext = getEventContext(eventManager,request);
            JpaRepository<Expediente> expedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());
            StateEventValidator stateEventValidator = getStateEventValidator(expediente.getTipoExpediente());
            StateEnum stateEnum = new StateEnum(ReflectionUtil.getEnumConstant(eventManager.getStateClass(), expediente.getCodeState()));

            if (eventName.equals(CommonEvent.EXIT.name())) {
                response.setSignal("refresh-app", null);
                return;
            }

            if ((stateEnum.getEvents().contains(eventName)==false)) {
                throw new RuntimeException("El evento '" + eventName + "' no es válido para el estado '" + expediente.getCodeState() + "'");
            }


            if (((eventName.equals(CommonEvent.DELETE.name()))==false) ) {
                BeanValidationRules beanValidationRules = getBeansValidationRules(stateEventValidator, expediente.getCodeState(), eventName);

                Map<String,Object> allowProperties = AllowPropertiesFactory.getAllowProperties(beanValidationRules.getFieldValidationRules());
                populateExpedienteFromActionRequest(expediente, request, eventName, eventContext, allowProperties);


                ValidatorEngine validatorEngine = new ValidatorEngine();
                BusinessMessages businessMessages = validatorEngine.validate(expediente, beanValidationRules);
                if (businessMessages.isValid()==false) {
                    JPA.em().detach(expediente);
                    AxelorViewUtil.doResponseBusinessMessages(response, businessMessages);
                    return;
                }
            }


            String originalState = expedienteOriginal.getCodeState();
            eventManager.triggerEvent(eventName, expediente, expedienteOriginal, eventContext);
            if (eventName.equals(CommonEvent.DELETE.name())) {
                removeExpediente(expedienteRepository, expediente);
                response.setSignal("refresh-app", null);
            } else {
                String newState = expediente.getCodeState();
                if (newState.equals(originalState) == false) {
                    addHistorialEstado(expediente, eventName);
                    eventManager.onEnterState(expediente, eventContext);
                }
                saveExpediente(expedienteRepository, expediente);

                String viewName = eventManager.getViewName(expediente, eventContext);
                AxelorViewUtil.doResponseViewForm(response, viewName, eventManager.getModelClass(), expediente, getTabName(expediente), eventContext.getProfile().name());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @CallMethod
    public void viewExpediente(ActionRequest request, ActionResponse response) {
        try {
            Expediente expediente=getExpedienteFromDB(request);
            Expediente expedienteOriginal=(Expediente) BeanMapperModel.getEntityCloned(expediente.getClass(), expediente);
            EventManager eventManager=getEventManager(expediente.getTipoExpediente());
            EventContext eventContext = getEventContext(eventManager,request);
            String viewName = eventManager.getViewName(expediente, eventContext);

            AxelorViewUtil.doResponseViewForm(response,viewName,eventManager.getModelClass(),expediente,getTabName(expediente),eventContext.getProfile().name());

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @CallMethod
    public void validateChild(ActionRequest request, ActionResponse response) {
        try {
            Map<String,Object> context = getActionRequestContext(request);
            Class<? extends Model> beanClass =(Class<? extends Model>) Class.forName((String)context.get("_model"));
            Model bean=findModel(beanClass, objectToLong(context.get("id")));
            String validateProperty=(String)((Map<String,Object>) context.get("_parent")).get("_source");
            String methodName="get"+TextUtil.toFirstsLetterToUpperCase(validateProperty);
            Expediente expediente=getExpedienteParentFromDB(request);
            TipoExpediente tipoExpediente=expediente.getTipoExpediente();

            StateEventValidator stateEventValidator = getStateEventValidator(tipoExpediente);
            List<BeanValidationRules> beansValidationRules = getBeansValidationRules(stateEventValidator, expediente.getCodeState());
            List<FieldValidationRules> fieldsValidationRules=getFieldsValidationRules(beansValidationRules,methodName);

            Map<String,Object> allowProperties = AllowPropertiesFactory.getAllowProperties(fieldsValidationRules);
            BeanMapperModel.copyMapToEntity(beanClass, context, bean, allowProperties);

            ValidatorEngine validatorEngine = new ValidatorEngine();
            BusinessMessages businessMessages = validatorEngine.validate(bean, fieldsValidationRules);
            JPA.em().detach(bean);
            AxelorViewUtil.doResponseBusinessMessages(response, businessMessages);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /*******************************************************************/
    /********************** Funciones de Negocio  **********************/
    /*******************************************************************/

    private static void addHistorialEstado(Expediente expediente, String eventName) {
        ExpedienteHistorialEstados historialEstado = new ExpedienteHistorialEstados();
        historialEstado.setCodeState(expediente.getCodeState());
        historialEstado.setNameState(TextUtil.getHumanCaseFromScreamingSnakeCase(expediente.getCodeState()));
        historialEstado.setCodeEvent((eventName!=null)?eventName:"");
        historialEstado.setNameEvent((eventName!=null)?TextUtil.getHumanCaseFromScreamingSnakeCase(eventName):"");
        historialEstado.setFecha(LocalDateTime.now());
        expediente.addHistorialEstado(historialEstado);
    }


    private void updateName(Expediente expediente) {
        expediente.setName(expediente.getTipoExpediente().getName());
    }


    private void assertValidState(Expediente expediente,Class<? extends Enum> enumClass) {
        String stateCode=expediente.getCodeState();
        boolean isValid = Arrays.stream(enumClass.getEnumConstants()).anyMatch(enumConstant -> stateCode.equals(enumConstant.name()));

        if (isValid==false) {
            throw new IllegalArgumentException("Invalid state code '" + stateCode + "'  "+enumClass.getSimpleName());
        }
    }

    private String getTabName(Expediente expediente) {
        return expediente.getNumeroExpediente()+"-"+expediente.getTipoExpediente().getName();
    }




    /*******************************************************************/
    /*************** Obtener los datos del ActionRequest ***************/
    /*******************************************************************/

    private TipoExpediente getTipoExpediente(ActionRequest request) {
        long id=objectToLong(getActionRequestContext(request).get("id"));

        TipoExpediente tipoExpediente=findTipoExpediente(tipoExpedienteRepository,id);

        return tipoExpediente;
    }



    private Expediente getExpedienteFromDB(ActionRequest request) {
        long id=objectToLong(getActionRequestContext(request).get("id"));

        JpaRepository<Expediente> expedienteRepository =getJpaRepository(id);

        Expediente expediente=findExpediente(expedienteRepository,id);

        if (expediente==null) {
            throw new RuntimeException("No existe el expediente con id: " + id);
        }

        return expediente;
    }

    private Expediente getExpedienteParentFromDB(ActionRequest request) {
        long id=objectToLong(((Map<String,Object>)(getActionRequestContext(request).get("_parent"))).get("id"));

        JpaRepository<Expediente> expedienteRepository =getJpaRepository(id);

        Expediente expediente=findExpediente(expedienteRepository,id);

        if (expediente==null) {
            throw new RuntimeException("No existe el expediente con id: " + id);
        }

        return expediente;
    }

    private void populateExpedienteFromActionRequest(Expediente expediente, ActionRequest request,String eventName, EventContext eventContext, Map<String,Object> allowProperties) {
        if (eventName!=null) {
            BeanMapperModel.copyMapToEntity(expediente.getClass(), getActionRequestContext(request), expediente, allowProperties);
        }
    }


    private String getEventName(ActionRequest request) {

        String eventName=(String)getActionRequestContext(request).get("_signal");

        if (eventName==null) {
            throw new RuntimeException("eventName is null");
        }

        return eventName;
    }


    public <T extends Enum<T>> EventContext<T> getEventContext(EventManager eventManager, ActionRequest request) {
        try {
            String profileName = (String) getActionRequestContext(request).get("_profile");
            if (profileName == null) {
                throw new RuntimeException("profileName is null");
            }

            Enum profile = Enum.valueOf(eventManager.getProfileClass(), profileName);
            return new EventContext<>(profile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /*******************************************************************/
    /******************* Funciones de Acceso a datos *******************/
    /*******************************************************************/


    private <T extends Expediente> void saveExpediente(JpaRepository<T> jpaRepository, T expediente) {
        jpaRepository.save(expediente);
    }

    private <T extends Expediente> void removeExpediente(JpaRepository<T> jpaRepository, T expediente) {
        jpaRepository.remove(expediente);
    }

    private <T extends Expediente> T findExpediente(JpaRepository<T> jpaRepository, long id) {
        return jpaRepository.find(id);
    }

    private <T extends TipoExpediente> T findTipoExpediente(JpaRepository<T> jpaRepository, long id) {
        return jpaRepository.find(id);
    }

    private Model findModel(Class<? extends Model> classModel, Long id) {
        try {
            Model model;
            if (id==null) {
                model=classModel.getConstructor().newInstance();
            } else {
                String fqcnRepositoryClass="com.educaflow.apps.expedientes.db.repo."+classModel.getSimpleName()+"Repository";
                Class<? extends JpaRepository> repositoryClass = (Class<? extends JpaRepository>) Class.forName(fqcnRepositoryClass);
                JpaRepository<?> repository = Beans.get(repositoryClass);
                model=repository.find(objectToLong(id));
            }

            return model;
        } catch (Exception ex) {
            throw new RuntimeException("Error al encontrar el modelo: " + classModel.getName() + " con id: " + id, ex);
        }
    }

    public Profile getProfile(String profileName) {
        ProfileRepository profileRepository = Beans.get(ProfileRepository.class);

        Profile profile = profileRepository.findByCode(profileName);
        if (profile == null) {
            throw new IllegalArgumentException("El Profile con nombre '" + profileName + "' no existe.");
        }
        return profile;
    }


    /*********************************************************************/
    /********************** Funciones de Validación **********************/
    /*********************************************************************/

    private StateEventValidator getStateEventValidator(TipoExpediente tipoExpediente) {
        try {
            if (tipoExpediente == null) {
                throw new RuntimeException("No existe el tipo del expediente a crear.");
            }
            String fqcnEventManager = tipoExpediente.getFqcnEventManager();
            if (fqcnEventManager == null || fqcnEventManager.isEmpty()) {
                throw new RuntimeException("No existe el fqcnEventManager para el tipo de expediente: " + tipoExpediente.getName());
            }

            int ultimoPunto = fqcnEventManager.lastIndexOf('.');
            if (ultimoPunto == -1) {
                throw new RuntimeException("El fqcnEventManager no tiene un punto: " + tipoExpediente.getFqcnEventManager());
            }

            String fqcnStateEventValidation = fqcnEventManager.substring(0, ultimoPunto) + ".StateEventValidator";

            Class<StateEventValidator> stateEventValidationClass = (Class<StateEventValidator>) Class.forName(fqcnStateEventValidation);

            StateEventValidator stateEventValidator = Beans.get(stateEventValidationClass);

            return stateEventValidator;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private BeanValidationRules getBeansValidationRules(StateEventValidator stateEventValidator, String state, String eventName) {
        try {
            String methodName = "getForState" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, state) + "InEvent" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, eventName);
            Method method = ReflectionUtil.getMethod(stateEventValidator.getClass(), methodName, BeanValidationRules.class, BeanValidationRulesForStateAndEvent.class, new Class<?>[]{});
            if (method==null) {
                throw new RuntimeException("No se ha encontrado el método: " + methodName + " en la clase: " + stateEventValidator.getClass().getName());
            }
            Object result = method.invoke(stateEventValidator);
            if (result == null) {
                throw new RuntimeException("No se han encontrado las reglas de validación para el estado: " + state + " y el evento: " + eventName);
            }


            BeanValidationRules beanValidationRules = (BeanValidationRules) result;

            return beanValidationRules;
        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener las reglas de validación para el estado: " + state + " y el evento: " + eventName + " en " + stateEventValidator.getClass().getName(), ex);
        }
    }

    private List<BeanValidationRules> getBeansValidationRules(StateEventValidator stateEventValidator, String state) {
        try {
            List<BeanValidationRules> beansValidationRules=new ArrayList<>();
            String methodName = "getForState" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, state) + "InEvent";


            for (Method method : stateEventValidator.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith(methodName)) {
                    if (method.isAnnotationPresent(BeanValidationRulesForStateAndEvent.class)) {
                        BeanValidationRules beanValidationRules=(BeanValidationRules)method.invoke(stateEventValidator);
                        if (beanValidationRules == null) {
                            throw new RuntimeException("El método retorno null:" + method.getName());
                        }
                        beansValidationRules.add(beanValidationRules);
                    }
                }
            }

            if (beansValidationRules.isEmpty()) {
                throw new RuntimeException("No se han encontrado las reglas de validación para el estado: " + state);
            }

            return beansValidationRules;

        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener las reglas de validación para el estado: " + state + " en " + stateEventValidator.getClass().getName(), ex);
        }
    }

    private List<FieldValidationRules> getFieldsValidationRules(List<BeanValidationRules> beansValidationRules,String methodName) {
        List<FieldValidationRules> fieldsValidationRules=new ArrayList<>();
        for(BeanValidationRules rules:beansValidationRules) {
            for(FieldValidationRules fieldValidationRules:rules.getFieldValidationRules()) {
                if (fieldValidationRules.getMethodField().getName().equals(methodName)) {
                    for(ValidationRule validationRule:fieldValidationRules.getValidationRules()) {
                        if ((validationRule instanceof FieldValidationRules)) {
                            fieldsValidationRules.add((FieldValidationRules)validationRule);
                        }
                    }
                }
            }
        }

        return fieldsValidationRules;
    }


    /*******************************************************************/
    /********************** Funciones de Utilidad **********************/
    /*******************************************************************/

    private static  EventManager getEventManager(TipoExpediente tipoExpediente) {
        try {
            if (tipoExpediente == null) {
                throw new RuntimeException("No existe el tipo del expediente a crear.");
            }
            String fqcnEventManager = tipoExpediente.getFqcnEventManager();
            if (fqcnEventManager == null || fqcnEventManager.isEmpty()) {
                throw new RuntimeException("No existe el fqcnEventManager para el tipo de expediente: " + tipoExpediente.getName());
            }
            Class<EventManager> eventManagerClass = (Class<EventManager>) Class.forName(tipoExpediente.getFqcnEventManager());

            EventManager eventManager = (EventManager) Beans.get(eventManagerClass);

            return eventManager;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<String,Object> getActionRequestContext(ActionRequest request) {
         Map<String,Object> requestcontext=(Map<String,Object>) request.getData().get("context");

         if (requestcontext == null) {
             throw new RuntimeException("requestcontext es null");
         }
         return (Map<String,Object>) requestcontext;
    }


    /**
     * Obtiene el Repository de un expediente en función del id del expediente.
     * Se usa este método porque de otra forma se retornaría el Repositorio de Expediente y no del expdiente en concreto.
     * @param idExpediente
     * @return
     */
    private JpaRepository<Expediente> getJpaRepository(long idExpediente) {
        JpaRepository<Expediente> onlyExpedienteRepository = AxelorDBUtil.getRepository(Expediente.class);
        Expediente expediente=onlyExpedienteRepository.find(idExpediente);
        EventManager eventManager=getEventManager(expediente.getTipoExpediente());
        JpaRepository<Expediente> realExpedienteRepository = AxelorDBUtil.getRepository(eventManager.getModelClass());
        JPA.em().detach(expediente);

        return realExpedienteRepository;
    }


    private Long objectToLong(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return ((Number) obj).longValue();
        }
    }


    private Enum<?> getInitialState(Class<? extends Enum> stateEnumClass) {
        Enum<?> initialState=null;
        Enum<?>[] states = stateEnumClass.getEnumConstants();

        for (Enum<?> state : states) {
            StateEnum stateEnum=new StateEnum(state);

            if (stateEnum.isInitial()) {
                if (initialState != null) {
                    throw new RuntimeException("Hay más de un estado inicial en la clase: " + stateEnumClass.getName());
                }
                initialState=state;
            }
        }

        if (initialState == null) {
            throw new RuntimeException("No se ha encontrado el estado inicial en la clase: " + stateEnumClass.getName());
        }

        return initialState;
    }

}
