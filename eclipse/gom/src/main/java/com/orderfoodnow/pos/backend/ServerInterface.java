package com.orderfoodnow.pos.backend;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.orderfoodnow.pos.shared.Cart;
import com.orderfoodnow.pos.shared.menu.Menu;
import com.orderfoodnow.pos.shared.order.CustomerProfile;
import com.orderfoodnow.pos.shared.staff.Employee;

public interface ServerInterface extends Remote {

	public Employee authenticateUser(String userId, String password) throws RemoteException, SQLException;

	public Menu getMenu() throws RemoteException;

	public long getTimestamp() throws RemoteException;

	public Map<Integer, Employee> refreshIdToEmployee() throws RemoteException, SQLException;

	public Map<String, Employee> refreshInitialsToActiveEmployee() throws RemoteException, SQLException;

	public Cart processCart(Cart cart) throws RemoteException, SQLException;

	public void stashCart(Cart cart) throws RemoteException;

	public List<Cart> findCartByOrderNumber(int orderNumber) throws RemoteException;

	public List<Cart> findCartByCustomerName(String customerName) throws RemoteException; 

	public List<Cart> findCartByPhoneNumber(String phoneNumber) throws RemoteException;

	public Cart findStashedCartByPhoneNumbe(String phoneNumber) throws RemoteException;

	public List<Cart> getOrderingHistory(String phoneNumber, int orderCount) throws RemoteException, SQLException;

	public CustomerProfile findCustomer(String phoneNumber) throws RemoteException, SQLException;

	public List<Cart> getUnsettledCarts() throws RemoteException;

	public List<Cart> getOutstandingCarts() throws RemoteException;

	public Map<String, Cart> getStashedCarts() throws RemoteException;

	public List<Cart> getCustomerArrivedCarts() throws RemoteException;

	public int[] getAllOrderIntervalTotals() throws RemoteException;

	public void settleCarts(int employeeId) throws RemoteException, SQLException;

	public void shutdown() throws RemoteException;
}
