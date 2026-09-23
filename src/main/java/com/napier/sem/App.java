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
            // Load Database driver
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
                // Close connection
                con.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     * Get an employee by employee number.
     */
    public Employee getEmployee(int ID)
    {
        try
        {
            Statement stmt = con.createStatement();

            String strSelect =
                    "SELECT emp_no, first_name, last_name "
                            + "FROM employees "
                            + "WHERE emp_no = " + ID;

            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next())
            {
                Employee emp = new Employee();

                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");

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

    public ArrayList<Employee> getSalariesByRole(String title)
    {
        ArrayList<Employee> employees = new ArrayList<>();

        try
        {
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, " +
                            "employees.last_name, salaries.salary " +
                            "FROM employees, salaries, titles " +
                            "WHERE employees.emp_no = salaries.emp_no " +
                            "AND employees.emp_no = titles.emp_no " +
                            "AND salaries.to_date = '9999-01-01' " +
                            "AND titles.to_date = '9999-01-01' " +
                            "AND titles.title = ? " +
                            "ORDER BY employees.emp_no ASC";

            PreparedStatement stmt = con.prepareStatement(strSelect);

            stmt.setString(1, title);

            ResultSet rset = stmt.executeQuery();

            while (rset.next())
            {
                Employee emp = new Employee();

                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.salary = rset.getInt("salary");
                emp.title = title;

                employees.add(emp);
            }

            return employees;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salaries by role");
            return employees;
        }
    }

    public void displayEmployee(Employee emp)
    {
        if (emp != null)
        {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept_name + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }

    public void displaySalariesByRole(ArrayList<Employee> employees)
    {
        for (Employee emp : employees)
        {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + " "
                            + emp.salary);
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


        ArrayList<Employee> employees =
                a.getSalariesByRole("Engineer");

        a.displaySalariesByRole(employees);

        a.disconnect();
    }
}