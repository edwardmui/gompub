package com.orderfoodnow.pos.frontend;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.orderfoodnow.pos.shared.Configuration;
import com.orderfoodnow.pos.shared.staff.Employee;
import com.orderfoodnow.pos.shared.staff.Role;

public class PermissionUtil {
	private static final Logger logger = Logger.getLogger(PermissionUtil.class.getName());

	public static boolean hasPermission(PermissionRequiredType permissionRequiredType, Employee employee) {
		logger.finest("Entered");
		logger.finer("permissionRequiredType=" + permissionRequiredType + ", actionName="
				+ permissionRequiredType.getActionName());

		List<Map<String, List<String>>> actionPermission;
		try {
			actionPermission = Configuration.getActionPermission();
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Exception: " + e.getMessage());
			return false;
		}

		for (Map<String, List<String>> actions : actionPermission) {
			for (String actionKey : actions.keySet()) {
				logger.finer("actionKey=" + actionKey + ", actionName=" + permissionRequiredType.getActionName());

				if (actionKey.equals(permissionRequiredType.getActionName())) {
					logger.finer(
							"found actionKey=" + actionKey + ", actionName=" + permissionRequiredType.getActionName());

					List<String> permittedRoles = actions.get(actionKey);
					List<Role> employeeRoles = employee.getRoles();

					logger.finer("permittedRoles=" + permittedRoles);
					logger.finer("employeeRoles=" + employeeRoles);
					for (Role employeeRole : employeeRoles) {
						if (permittedRoles.contains(employeeRole.getRoleType().getConfigurationName())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

}
