public class BeforAop3 implements BaseInterface {
    @Override
    public Object Excute(Method method, Object object, Object[] objects) {
        return synchronizedMethod(method,object,objects);
    }
    public synchronized Object synchronizedMethod(Method method, Object object, Object[] objects){
        try {
            return method.invoke(object,objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
