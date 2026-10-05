package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

public class App
{
    private Connection con = null;

    public void connect(String location, int delay)
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");
            try
            {
                if (delay > 0)
                {
                    Thread.sleep(delay);
                }

                con = DriverManager.getConnection(
                        "jdbc:mysql://" + location + "/employees?allowPublicKeyRetrieval=true&useSSL=false",
                        "root",
                        "example"
                );

                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());
            }
            catch (InterruptedException ie)
            {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    public void disconnect()
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Exercise Requirement: Get an employee by ID, including their department and department manager.
     */
    public Employee getEmployee(int ID)
    {
        try
        {
            // Joins employee, department, and current manager details
            String strSelect =
                    "SELECT emp.emp_no, emp.first_name, emp.last_name, " +
                            "d.dept_no, d.dept_name, " +
                            "mgr.emp_no AS mgr_emp_no, mgr.first_name AS mgr_first_name, mgr.last_name AS mgr_last_name " +
                            "FROM employees emp " +
                            "LEFT JOIN dept_emp de ON emp.emp_no = de.emp_no AND de.to_date = '9999-01-01' " +
                            "LEFT JOIN departments d ON de.dept_no = d.dept_no " +
                            "LEFT JOIN dept_manager dm ON d.dept_no = dm.dept_no AND dm.to_date = '9999-01-01' " +
                            "LEFT JOIN employees mgr ON dm.emp_no = mgr.emp_no " +
                            "WHERE emp.emp_no = ?";

            PreparedStatement stmt = con.prepareStatement(strSelect);
            stmt.setInt(1, ID);

            ResultSet rset = stmt.executeQuery();

            if (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");

                // Populate Department if present
                if (rset.getString("dept_no") != null)
                {
                    Department dept = new Department();
                    dept.dept_no = rset.getString("dept_no");
                    dept.dept_name = rset.getString("dept_name");

                    // Populate Department Manager if present
                    if (rset.getObject("mgr_emp_no") != null)
                    {
                        Employee mgr = new Employee();
                        mgr.emp_no = rset.getInt("mgr_emp_no");
                        mgr.first_name = rset.getString("mgr_first_name");
                        mgr.last_name = rset.getString("mgr_last_name");

                        dept.manager = mgr;
                        emp.manager = mgr; // Also assign manager directly to employee
                    }

                    emp.dept = dept;
                }

                return emp;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * Exercise Requirement: Get an employee based on their first name and last name.
     */
    public Employee getEmployee(String first_name, String last_name)
    {
        try
        {
            String strSelect =
                    "SELECT emp.emp_no, emp.first_name, emp.last_name, " +
                            "d.dept_no, d.dept_name, " +
                            "mgr.emp_no AS mgr_emp_no, mgr.first_name AS mgr_first_name, mgr.last_name AS mgr_last_name " +
                            "FROM employees emp " +
                            "LEFT JOIN dept_emp de ON emp.emp_no = de.emp_no AND de.to_date = '9999-01-01' " +
                            "LEFT JOIN departments d ON de.dept_no = d.dept_no " +
                            "LEFT JOIN dept_manager dm ON d.dept_no = dm.dept_no AND dm.to_date = '9999-01-01' " +
                            "LEFT JOIN employees mgr ON dm.emp_no = mgr.emp_no " +
                            "WHERE emp.first_name = ? AND emp.last_name = ?";

            PreparedStatement stmt = con.prepareStatement(strSelect);
            stmt.setString(1, first_name);
            stmt.setString(2, last_name);

            ResultSet rset = stmt.executeQuery();

            if (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");

                if (rset.getString("dept_no") != null)
                {
                    Department dept = new Department();
                    dept.dept_no = rset.getString("dept_no");
                    dept.dept_name = rset.getString("dept_name");

                    if (rset.getObject("mgr_emp_no") != null)
                    {
                        Employee mgr = new Employee();
                        mgr.emp_no = rset.getInt("mgr_emp_no");
                        mgr.first_name = rset.getString("mgr_first_name");
                        mgr.last_name = rset.getString("mgr_last_name");

                        dept.manager = mgr;
                        emp.manager = mgr;
                    }

                    emp.dept = dept;
                }

                return emp;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details by name");
            return null;
        }
    }

    /**
     * Exercise Requirement: Get a department and attach its active department manager.
     */
    public Department getDepartment(String dept_name)
    {
        try
        {
            String strSelect =
                    "SELECT d.dept_no, d.dept_name, " +
                            "e.emp_no, e.first_name, e.last_name " +
                            "FROM departments d " +
                            "LEFT JOIN dept_manager dm ON d.dept_no = dm.dept_no AND dm.to_date = '9999-01-01' " +
                            "LEFT JOIN employees e ON dm.emp_no = e.emp_no " +
                            "WHERE d.dept_name = ?";

            PreparedStatement stmt = con.prepareStatement(strSelect);
            stmt.setString(1, dept_name);

            ResultSet rset = stmt.executeQuery();

            if (rset.next())
            {
                Department dept = new Department();
                dept.dept_no = rset.getString("dept_no");
                dept.dept_name = rset.getString("dept_name");

                // Construct and attach the manager Employee object
                if (rset.getObject("emp_no") != null)
                {
                    Employee mgr = new Employee();
                    mgr.emp_no = rset.getInt("emp_no");
                    mgr.first_name = rset.getString("first_name");
                    mgr.last_name = rset.getString("last_name");
                    dept.manager = mgr;
                }

                return dept;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get department details");
            return null;
        }
    }

    public ArrayList<Employee> getSalariesByDepartment(Department dept)
    {
        if (dept == null)
        {
            System.out.println("No department supplied");
            return null;
        }

        try
        {
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries, dept_emp, departments "
                            + "WHERE employees.emp_no = salaries.emp_no "
                            + "AND employees.emp_no = dept_emp.emp_no "
                            + "AND dept_emp.dept_no = departments.dept_no "
                            + "AND salaries.to_date = '9999-01-01' "
                            + "AND departments.dept_no = ? "
                            + "ORDER BY employees.emp_no ASC";

            PreparedStatement stmt = con.prepareStatement(strSelect);
            stmt.setString(1, dept.dept_no);

            ResultSet rset = stmt.executeQuery();

            ArrayList<Employee> employees = new ArrayList<>();
            while (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                emp.dept = dept;
                employees.add(emp);
            }
            return employees;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details by department");
            return null;
        }
    }

    public void printSalaries(ArrayList<Employee> employees)
    {
        if (employees == null)
        {
            System.out.println("No employees");
            return;
        }

        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));

        for (Employee emp : employees)
        {
            if (emp == null)
                continue;
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    public static void main(String[] args)
    {
        App a = new App();

        if (args.length < 1)
        {
            a.connect("localhost:3306", 5000);
        }
        else
        {
            a.connect(args[0], 30000);
        }

        // Test 1: Get Department & Manager
        Department dept = a.getDepartment("Sales");

        // Test 2: Get Salaries by Department
        ArrayList<Employee> employees = a.getSalariesByDepartment(dept);
        a.printSalaries(employees);

        // Test 3: Get Employee by Name
        Employee emp = a.getEmployee("Maja", "Lamba");
        if (emp != null)
        {
            System.out.println("\nFound Employee: " + emp.emp_no + " " + emp.first_name + " " + emp.last_name);
        }

        a.disconnect();
    }
}