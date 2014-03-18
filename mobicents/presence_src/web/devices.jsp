<!doctype html> 
<html> 
<head> 
<title>Registered devices</title> 
</head> 
<body> 
<%@ page import ="java.sql.*" %>
<%@ page import ="javax.sql.*" %>
<%@ page import ="org.apache.commons.lang3.*" %>
<%	
          String url = "jdbc:mysql://localhost:3306/";
          String dbName = "components";
          String driver = "com.mysql.jdbc.Driver";
          String userName = "presence";
          String password = "columbianyc";
          try {
          Class.forName(driver).newInstance();
          Connection connect = DriverManager.getConnection(url+dbName,userName,password);
          
          Statement statement = connect.createStatement();
      
      	  ResultSet resultSet = statement.executeQuery("select * from models");
      	  while(resultSet.next()) {
      	  	String uri = resultSet.getString("sip_uri");
      	  	String model = resultSet.getString("component_model");
      	  	
      	  	out.println("<h2>SIP URI: " + uri + "</h2></br>");
      	  	out.println("Component XML:<textarea style=\"border:none;\"> " + model + "</textarea></br></br>");
      	  } 
           
          connect.close();
          } catch (Exception e) {
          	e.printStackTrace();
          }          

 %> 
</body> 
</html> 