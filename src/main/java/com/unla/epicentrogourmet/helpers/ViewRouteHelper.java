package com.unla.epicentrogourmet.helpers;

public class ViewRouteHelper {
	/**** Views ****/
	//HOME
	public final static String INDEX = "home/index";

	//USER
	public final static String USER_LOGIN = "user/login";

	//ADMIN
	public final static String ADMIN_INDEX = "admin/index";

	//RESPONSABLE
	public final static String RESPONSABLE_INDEX = "responsable/index";

	//EMPLEADO
	public final static String EMPLEADO_INDEX = "empleado/index";
	public final static String PEDIDO_FORM = "empleado/pedido-form";
	public final static String PEDIDO_LIST = "empleado/pedidos";

	//REGISTRO
	public final static String REGISTRO_FORM = "registro/form";

	//PRUEBA EMAIL
	public static final String EMAILTESTFORM = "admin/emailTest";

	/**** Redirects ****/
	public final static String ROUTE = "/index";
	public final static String ADMIN_ROOT = "/admin/index";
	public final static String RESPONSABLE_ROOT = "/responsable/index";
	public final static String EMPLEADO_ROOT = "/empleado/index";
	public final static String PEDIDOS_ROOT = "/empleado/pedidos";
	public final static String LOGIN_REGISTRADO = "/login?registrado";

}
