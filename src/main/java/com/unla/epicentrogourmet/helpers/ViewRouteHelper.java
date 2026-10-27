package com.unla.epicentrogourmet.helpers;

public class ViewRouteHelper {
	/**** Views ****/
	//HOME
	public final static String INDEX = "home/index";

	//USER
	public final static String USER_LOGIN = "user/login";

	//ADMIN
	public final static String ADMIN_INDEX = "admin/index";
	public final static String ADMIN_FESTIVALES = "admin/festivales";
	public final static String ADMIN_FESTIVAL_UNIDADES = "admin/festival-unidades";
	public final static String ADMIN_CANON = "admin/canon";
	public final static String ADMIN_RECAUDACION = "admin/recaudacion";

	//RESPONSABLE
	public final static String RESPONSABLE_INDEX = "responsable/index";
	public final static String RESPONSABLE_CANON = "responsable/canon";
	public final static String RESPONSABLE_PLATO_ESTRELLA = "responsable/plato-estrella";
	public final static String RESPONSABLE_RECAUDACION = "responsable/recaudacion";
	public final static String RESPONSABLE_RECAUDACION_DIARIA = "responsable/recaudacion-diaria";
	public final static String RESPONSABLE_RENTABILIDAD = "responsable/rentabilidad";
	public final static String PLATO_LIST = "responsable/platos";
	public final static String PLATO_FORM = "responsable/plato-form";
	public final static String EMPLEADO_LIST = "responsable/empleados";
	public final static String EMPLEADO_FORM = "responsable/empleado-form";

	//EMPLEADO
	public final static String EMPLEADO_INDEX = "empleado/index";
	public final static String PEDIDO_FORM = "empleado/pedido-form";
	public final static String PEDIDO_LIST = "empleado/pedidos";
	public final static String CIERRE_CAJA = "empleado/cierre-caja";
	public final static String SUELDO = "empleado/sueldo";

	//REGISTRO
	public final static String REGISTRO_FORM = "registro/form";

	//PRUEBA EMAIL
	public final static String EMAIL_TEST_FORM = "responsable/emailTest";

	/**** Redirects ****/
	public final static String ROUTE = "/index";
	public final static String ADMIN_ROOT = "/admin/index";
	public final static String RESPONSABLE_ROOT = "/responsable/index";
	public final static String PLATOS_ROOT = "/responsable/platos";
	public final static String EMPLEADOS_ROOT = "/responsable/empleados";
	public final static String EMPLEADO_ROOT = "/empleado/index";
	public final static String PEDIDOS_ROOT = "/empleado/pedidos";
	public final static String LOGIN_REGISTRADO = "/login?registrado";
	public final static String EMAIL_TEST_ROOT = "/responsable/emailTest";


}
