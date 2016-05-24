/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package medistore;

import java.security.SecureRandom;
import javax.swing.*;
import java.sql.*;
import java.awt.event.*;
import java.awt.Desktop;
import com.mxrck.autocompleter.TextAutoCompleter;
import java.awt.Color;
import java.awt.Image;
import javax.swing.table.DefaultTableModel; 
import javax.imageio.ImageIO;
import java.util.Date;
import java.util.*;
import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import java.net.URI;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 *
 * @author Rohit Sharma
 */


public class home extends javax.swing.JFrame{

    /**
     * Creates new form home
     */
    

    @Override
    public void setIconImage(Image image) {
        super.setIconImage(image); //To change body of generated methods, choose Tools | Templates.
    }
    
    boolean active=false;   //variable to show whether the product is active or not
    
    boolean activity=false;
    boolean trial=false;
            
    public home() {
        initComponents();
        initAutoCompleter_jtextField();
        
        check_active();
       
        landing_setup();
        int show_newAddition_warning=0;
        
        //setting up the components of landing frame
        jTextField15.requestFocus(true);
        jButton12.setVisible(false);
    }
    
    private void registerMsg(){
        JOptionPane.showMessageDialog(null, "Please register the product with a valid key to use.");
    }
    private void connectionMsg(){
        JOptionPane.showMessageDialog(this, "Please check your internet connection.");
    }
    
    private void updateNote(){
        Connection con; ResultSet rs;
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                    con=(Connection)DriverManager.getConnection(connectionURL);

            Statement stmt=con.createStatement();
            rs = stmt.executeQuery("select NAME from NOTE");

            while(rs.next()){
                jComboBox4.addItem(rs.getString("NAME"));
            }

            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
    }
    private void check_active(){
        ResultSet rs;
        try {

            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
    
            Statement stmt=con.createStatement();
            //check wether the product is active or not
                
            boolean computerId_matches=false;
            rs=stmt.executeQuery("SELECT * FROM STOCKUP_ADMIN");
            while(rs.next()){
                if(rs.getString("COMPUTER_ID").equals(getSerialNumber()) || rs.getString("MAC_ID").equals(getSysDetails("mac")) ){
                    computerId_matches=true;
                }
            }
            
            if(computerId_matches){
                rs=stmt.executeQuery("SELECT * FROM ACTIVITY");
                while(rs.next()){
                    if(rs.getString("ACTIVE").equals("ACTIVE")){
                        activity=true;
                        
                        jPanel50.setVisible(false);
                    }
                }
            }
            else{
                rs=stmt.executeQuery("SELECT * FROM TRIAL_DETAILS");
                while(rs.next()){
                    if(Integer.valueOf(rs.getString("ATTEMPTS"))>0){
                        trial=true;          
                        jTabbedPane2.setSelectedIndex(4);
                        jTabbedPane4.setSelectedIndex(1);
                        jTabbedPane5.setSelectedIndex(0);
                        jPanel50.setVisible(true);
                    }
                    System.out.println("attmpts::"+rs.getString("ATTEMPTS"));
                }
            }
            
            
            if(activity==true||trial==true){
                active=true;
            }
            else{
                jPanel50.setVisible(true);
            }
            
            
            //set the licensee details 
            rs=stmt.executeQuery("SELECT * FROM STOCKUP_ADMIN");

            while(rs.next()){
                jLabel220.setText(rs.getString("USERNAME"));
                jLabel223.setText(rs.getString("USERMAIL"));
                jLabel225.setText("Not Registered with Sybero");
                jLabel227.setText(rs.getString("productId"));
                String str=rs.getString("productKey");
                
                jLabel229.setText(str.substring(0, 4)+"-"+str.substring(4, 8)+"-"+str.substring(8, 12)+"-"+str.substring(12, 16));        
                System.out.println("am at 12185");
            }

            //set the startup page
            rs=stmt.executeQuery("SELECT START_PAGE FROM STOCKUP_ADMIN");
            int i=0;
            while(rs.next()){
                i=Integer.valueOf(rs.getString("Start_page"));
            }
            
            if(i==0||i==1){
                jTabbedPane2.setSelectedIndex(0);
            }
            else if(i==2){
                jTabbedPane2.setSelectedIndex(2);
            }
            else if(i==3){
                jTabbedPane2.setSelectedIndex(1);
            }
            else if(i==4){
                jTabbedPane2.setSelectedIndex(5);
            }
            else if(i==5){
                jTabbedPane2.setSelectedIndex(3);
            }
            else if(i==6){
                jTabbedPane2.setSelectedIndex(4);
            }
            
            
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.getStackTrace();
        }
    }
    
    private void landing_setup(){
        Connection con; ResultSet rs;
        try {
            Date myDate=new Date();
            
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            con=(Connection)DriverManager.getConnection(connectionURL);
            System.out.println("Connecttion SuccessFul landing_setup");

            Statement stmt=con.createStatement();

            //check for last day sales details and update lastday sales
            rs = stmt.executeQuery("SELECT DATE FROM TODAY_SALE");
            
            boolean outdated=false;
            String last_date="";
            while(rs.next()){
                if(!rs.getString("DATE").equals(new SimpleDateFormat("dd/MM/yyyy").format(myDate))){
                    outdated=true;
                }
            }
            
            if(outdated){
                rs=stmt.executeQuery("SELECT DATE FROM LASTDAY_SALE");
                if(rs.next()){
                    last_date=rs.getString("DATE");
                }
                if(!last_date.equals("")){
                    DatabaseMetaData dbm = con.getMetaData();
                    String table_name="DAY"+last_date.substring(0, 2)+last_date.substring(3, 5)+last_date.substring(6, 10);

                    ResultSet tables = dbm.getTables(null, null, table_name, null);
                    if (tables.next()) {
                      System.out.println("table present");
                    }
                    else {
                        String sql = "CREATE TABLE "+table_name+" (NAME varchar(255),QUANTITY INTEGER,PRICE DOUBLE,TIME varchar(255),DATE varchar(255))"; 
                        stmt.executeUpdate(sql);
                    }

                    stmt.executeUpdate("INSERT INTO "+table_name+" SELECT * FROM LASTDAY_SALE");
                    stmt.executeUpdate("TRUNCATE TABLE LASTDAY_SALE");
                    
                }
                
                stmt.executeUpdate("INSERT INTO LASTDAY_SALE SELECT * FROM TODAY_SALE");
                stmt.executeUpdate("TRUNCATE TABLE TODAY_SALE");
            }
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.getStackTrace();
        }
        
        
    }
    
    
    private void update_item_table(){
        DefaultTableModel model15=(DefaultTableModel)jTable15.getModel();
        DefaultTableModel model18=(DefaultTableModel)jTable18.getModel();
        model18.setRowCount(0);
        int total_quant=0;
        double total_amt=0.0;
        
        int t=model15.getRowCount();
        boolean added=false;
        for (int i=0;i<t;i++){
            added=false;
            String name=(String)model15.getValueAt(i, 0);
            int quantity=(int)model15.getValueAt(i, 1);
            double amount=quantity*(double)model15.getValueAt(i, 2);
            total_quant=total_quant+quantity;
            total_amt=total_amt+amount;
            
            for(int j=0;j<model18.getRowCount();j++){
                String name1=(String)model18.getValueAt(j, 0);
                int quantity1=(int)model18.getValueAt(j, 1);
                double amount1=(double)model18.getValueAt(j, 2);
                
                if(name.equals(name1)){
                    model18.setValueAt(quantity+quantity1, j, 1);
                    model18.setValueAt(amount+amount1, j, 2);
                    added=true;
                }
            }
            if(!added){
                model18.addRow(new Object[] {name,quantity,amount});
            }
            System.out.println(model15.getValueAt(i, 0));
        }
        jLabel186.setText(String.valueOf(total_quant));
        jLabel180.setText(String.valueOf(total_amt));
    }
    
    
    private void initAutoCompleter_jtextField(){
        Connection con; ResultSet rs;
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            con=(Connection)DriverManager.getConnection(connectionURL);
            System.out.println("Connecttion SuccessFul");

            Statement stmt=con.createStatement();
            String query="select name from MED_DETAILS";
            rs = stmt.executeQuery(query);

            System.out.println("retrieve succesfully");

            TextAutoCompleter complete15=new TextAutoCompleter(jTextField15);
            TextAutoCompleter complete14=new TextAutoCompleter(jTextField14);
            TextAutoCompleter complete1=new TextAutoCompleter(jTextField1);
            TextAutoCompleter complete2=new TextAutoCompleter(jTextField2);
            TextAutoCompleter complete17=new TextAutoCompleter(jTextField17);

            while(rs.next()){
                complete15.addItem(rs.getString("name"));
                complete14.addItem(rs.getString("name"));
                complete1.addItem(rs.getString("name"));
                complete2.addItem(rs.getString("name"));
                complete17.addItem(rs.getString("name"));
            }
            
           
            
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
    }
    
   private void refresh_jTable16(){
       try {
            /*String driver = "org.apache.derby.jdbc.EmbeddedDriver";
            String dbName = "MediCellarDB";
            String connectionURL = "jdbc:derby:" + dbName + ";create=true";

            try{
                Class.forName(driver);
            }catch(Exception e){
                e.getStackTrace();
            }*/
           String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
            System.out.println("Connecttion SuccessFul");

            Statement stmt=con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from SHORTLIST");
            
            DefaultTableModel model=(DefaultTableModel) jTable16.getModel();
            model.setNumRows(0);
            while(rs.next()){
                model.addRow(new Object[] {rs.getString("NAME"),rs.getString("DATE"),"NA","NA"});
            }

            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
   }
   
   
   private void refresh_jTable2(){
        try {
            
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con2=(Connection)DriverManager.getConnection(connectionURL);
            System.out.println("Connecttion SuccessFul");

            Statement stmt=con2.createStatement();
            String query="select NAME from OUT_OF_STOCK";
            ResultSet rs2 = stmt.executeQuery(query);

            DefaultTableModel model = (DefaultTableModel)jTable4.getModel();
            DefaultTableModel model1 = (DefaultTableModel)jTable2.getModel();
            
            model.setNumRows(0);
            model1.setNumRows(0);
            
            while(rs2.next()){
                model.addRow(new Object[] {rs2.getString("NAME"),"nothing","null","huh","hey 123"});
            }



            int n=jTable4.getRowCount();

            for(int i=0;i<n;i++){
                String name = (String)model.getValueAt(i,0);

                query="select * from MED_DETAILS";
                rs2=stmt.executeQuery(query);

                while(rs2.next()){
                    if(name.equals(rs2.getString("NAME"))){
                        model1.addRow(new Object[] {rs2.getString("NAME"),rs2.getString("CONFIG1"),rs2.getString("CONFIG2"),rs2.getString("CONFIG3"),rs2.getString("NOTE")});
                        System.out.println("success...");
                    }
                }
            }

            jLabel55.setText(String.valueOf(jTable2.getRowCount()));
            
            rs2.close();
            stmt.close();
            con2.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
   }
   
    private void refresh_jTable3(){
        try {
           String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con1=(Connection)DriverManager.getConnection(connectionURL);
            
            Statement stmt=con1.createStatement();
            String query="select * from STOCK";
            ResultSet rs1 = stmt.executeQuery(query);


            DefaultTableModel model = (DefaultTableModel)jTable3.getModel();
            model.setNumRows(0);
            
            while(rs1.next()){
                model.addRow(new Object[] {rs1.getString("MED_NAME"),rs1.getInt("QUANTITY"),rs1.getInt("PRICE"),rs1.getString("LOCATION")});
            }
            
            jLabel54.setText(String.valueOf(model.getRowCount()));
            rs1.close();
            stmt.close();
            con1.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
   }
    private void refresh_jTable17(){
        try {
           String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
            
            Statement stmt=con.createStatement();

            ResultSet rs = stmt.executeQuery("select * from TODAY_SALE");
            int quantity=0;
            Double amount=0.0;
            String date="";
            DefaultTableModel model=(DefaultTableModel)jTable17.getModel();
            model.setRowCount(0);
            while(rs.next()){
                Double price=rs.getDouble("PRICE");
                int quant=rs.getInt("QUANTITY");
                Double amt=price*quant;
                quantity=quantity+quant;
                amount=amount+amt;
                model.addRow(new Object[] {rs.getString("NAME"),String.valueOf(price),String.valueOf(quant),String.valueOf(amt),rs.getString("TIME")});
            }
            
            jLabel144.setText(String.valueOf(quantity));
            jLabel148.setText(String.valueOf(amount));
            
            int row_num=model.getRowCount();
            
            for(int i=row_num;i<16;i++){
                model.addRow(new Object[] {"","","","",""});
            }
            
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.getStackTrace();
        }
    }
    
    private void refresh_jTable13(){
    try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
            System.out.println("Connecttion SuccessFul");

            Statement stmt=con.createStatement();

            ResultSet rs = stmt.executeQuery("select * from LASTDAY_SALE");
            int quantity=0;
            Double amount=0.0;
            String date="";
            DefaultTableModel model=(DefaultTableModel)jTable13.getModel();
            model.setRowCount(0);
            while(rs.next()){
                date=rs.getString("DATE");
                
                Double price=rs.getDouble("PRICE");
                int quant=rs.getInt("QUANTITY");
                
                Double amt=price*quant;
                
                quantity=quantity+quant;
                amount=amount+amt;
                model.addRow(new Object[] {rs.getString("NAME"),String.valueOf(price),String.valueOf(quant),String.valueOf(amt),rs.getString("TIME")});
            }
            
            jLabel135.setText(String.valueOf(quantity));
            jLabel138.setText(String.valueOf(amount));
            jLabel142.setText(date);
            
            int row_num=model.getRowCount();
            
            for(int i=row_num;i<16;i++){
                model.addRow(new Object[] {"","","","",""});
            }
            
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.getStackTrace();
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jScrollPane10 = new javax.swing.JScrollPane();
        jTable8 = new javax.swing.JTable();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTable9 = new javax.swing.JTable();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTable10 = new javax.swing.JTable();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel16 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        jLabel172 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jLabel200 = new javax.swing.JLabel();
        jPanel23 = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jPanel29 = new javax.swing.JPanel();
        jPanel27 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jLabel36 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        jTextField23 = new javax.swing.JTextField();
        jLabel56 = new javax.swing.JLabel();
        jPanel50 = new javax.swing.JPanel();
        jLabel237 = new javax.swing.JLabel();
        jLabel244 = new javax.swing.JLabel();
        jPanel53 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        jTextField13 = new javax.swing.JTextField();
        jTextField14 = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jTextField20 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jScrollPane14 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jScrollPane15 = new javax.swing.JScrollPane();
        jEditorPane2 = new javax.swing.JEditorPane();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel39 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jLabel123 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jTextField16 = new javax.swing.JTextField();
        jTextField19 = new javax.swing.JTextField();
        jTextField22 = new javax.swing.JTextField();
        jTextField15 = new javax.swing.JTextField();
        jLabel124 = new javax.swing.JLabel();
        jPanel28 = new javax.swing.JPanel();
        jLabel72 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel117 = new javax.swing.JLabel();
        jLabel118 = new javax.swing.JLabel();
        jLabel119 = new javax.swing.JLabel();
        jLabel120 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jTextField17 = new javax.swing.JTextField();
        jLabel90 = new javax.swing.JLabel();
        jButton20 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jLabel98 = new javax.swing.JLabel();
        jScrollPane19 = new javax.swing.JScrollPane();
        jTable12 = new javax.swing.JTable();
        jLabel84 = new javax.swing.JLabel();
        jLabel116 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jTextField21 = new javax.swing.JTextField();
        jTextField18 = new javax.swing.JTextField();
        jLabel101 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jButton18 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jLabel86 = new javax.swing.JLabel();
        jLabel113 = new javax.swing.JLabel();
        jLabel114 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel115 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        jLabel94 = new javax.swing.JLabel();
        jScrollPane16 = new javax.swing.JScrollPane();
        jEditorPane3 = new javax.swing.JEditorPane();
        jScrollPane17 = new javax.swing.JScrollPane();
        jEditorPane4 = new javax.swing.JEditorPane();
        jScrollPane18 = new javax.swing.JScrollPane();
        jEditorPane5 = new javax.swing.JEditorPane();
        jLabel173 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jLabel62 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel41 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        jLabel97 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        jLabel100 = new javax.swing.JLabel();
        jLabel121 = new javax.swing.JLabel();
        jLabel122 = new javax.swing.JLabel();
        jLabel125 = new javax.swing.JLabel();
        jLabel128 = new javax.swing.JLabel();
        jLabel129 = new javax.swing.JLabel();
        jLabel130 = new javax.swing.JLabel();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel32 = new javax.swing.JPanel();
        jLabel131 = new javax.swing.JLabel();
        jLabel132 = new javax.swing.JLabel();
        jLabel133 = new javax.swing.JLabel();
        jLabel135 = new javax.swing.JLabel();
        jLabel136 = new javax.swing.JLabel();
        jLabel137 = new javax.swing.JLabel();
        jLabel138 = new javax.swing.JLabel();
        jLabel139 = new javax.swing.JLabel();
        jLabel140 = new javax.swing.JLabel();
        jLabel141 = new javax.swing.JLabel();
        jLabel142 = new javax.swing.JLabel();
        jScrollPane20 = new javax.swing.JScrollPane();
        jTable13 = new javax.swing.JTable();
        jLabel143 = new javax.swing.JLabel();
        jLabel144 = new javax.swing.JLabel();
        jLabel145 = new javax.swing.JLabel();
        jLabel146 = new javax.swing.JLabel();
        jLabel147 = new javax.swing.JLabel();
        jLabel148 = new javax.swing.JLabel();
        jLabel149 = new javax.swing.JLabel();
        jScrollPane26 = new javax.swing.JScrollPane();
        jTable17 = new javax.swing.JTable();
        jPanel33 = new javax.swing.JPanel();
        jLabel150 = new javax.swing.JLabel();
        jLabel151 = new javax.swing.JLabel();
        jLabel152 = new javax.swing.JLabel();
        jLabel153 = new javax.swing.JLabel();
        jXDatePicker1 = new org.jdesktop.swingx.JXDatePicker();
        jLabel154 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jLabel155 = new javax.swing.JLabel();
        jLabel156 = new javax.swing.JLabel();
        jScrollPane22 = new javax.swing.JScrollPane();
        jTable15 = new javax.swing.JTable();
        jLabel157 = new javax.swing.JLabel();
        jLabel158 = new javax.swing.JLabel();
        jLabel159 = new javax.swing.JLabel();
        jLabel180 = new javax.swing.JLabel();
        jLabel181 = new javax.swing.JLabel();
        jLabel182 = new javax.swing.JLabel();
        jLabel186 = new javax.swing.JLabel();
        jScrollPane27 = new javax.swing.JScrollPane();
        jTable18 = new javax.swing.JTable();
        jLabel201 = new javax.swing.JLabel();
        jLabel202 = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        jScrollPane23 = new javax.swing.JScrollPane();
        jTable16 = new javax.swing.JTable();
        jLabel187 = new javax.swing.JLabel();
        jLabel188 = new javax.swing.JLabel();
        jLabel189 = new javax.swing.JLabel();
        jTextField24 = new javax.swing.JTextField();
        jLabel199 = new javax.swing.JLabel();
        jPanel34 = new javax.swing.JPanel();
        jLabel190 = new javax.swing.JLabel();
        jLabel191 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jScrollPane24 = new javax.swing.JScrollPane();
        jEditorPane6 = new javax.swing.JEditorPane();
        jLabel192 = new javax.swing.JLabel();
        jLabel193 = new javax.swing.JLabel();
        jLabel194 = new javax.swing.JLabel();
        jLabel195 = new javax.swing.JLabel();
        jLabel196 = new javax.swing.JLabel();
        jLabel197 = new javax.swing.JLabel();
        jLabel198 = new javax.swing.JLabel();
        jTextField25 = new javax.swing.JTextField();
        jLabel134 = new javax.swing.JLabel();
        jPanel42 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jLabel203 = new javax.swing.JLabel();
        jLabel204 = new javax.swing.JLabel();
        jLabel205 = new javax.swing.JLabel();
        jLabel206 = new javax.swing.JLabel();
        jLabel207 = new javax.swing.JLabel();
        jLabel208 = new javax.swing.JLabel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel43 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jLabel209 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox();
        jLabel210 = new javax.swing.JLabel();
        jButton23 = new javax.swing.JButton();
        jLabel211 = new javax.swing.JLabel();
        jLabel212 = new javax.swing.JLabel();
        jLabel213 = new javax.swing.JLabel();
        jLabel214 = new javax.swing.JLabel();
        jLabel215 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox();
        jXDatePicker2 = new org.jdesktop.swingx.JXDatePicker();
        jComboBox7 = new javax.swing.JComboBox();
        jLabel216 = new javax.swing.JLabel();
        jComboBox9 = new javax.swing.JComboBox();
        jPasswordField3 = new javax.swing.JPasswordField();
        jPasswordField4 = new javax.swing.JPasswordField();
        jPasswordField5 = new javax.swing.JPasswordField();
        jLabel245 = new javax.swing.JLabel();
        jLabel246 = new javax.swing.JLabel();
        jLabel247 = new javax.swing.JLabel();
        jLabel248 = new javax.swing.JLabel();
        jLabel249 = new javax.swing.JLabel();
        jLabel250 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jPanel45 = new javax.swing.JPanel();
        jLabel218 = new javax.swing.JLabel();
        jLabel221 = new javax.swing.JLabel();
        jLabel220 = new javax.swing.JLabel();
        jLabel222 = new javax.swing.JLabel();
        jLabel223 = new javax.swing.JLabel();
        jLabel224 = new javax.swing.JLabel();
        jLabel225 = new javax.swing.JLabel();
        jLabel226 = new javax.swing.JLabel();
        jLabel227 = new javax.swing.JLabel();
        jLabel228 = new javax.swing.JLabel();
        jLabel229 = new javax.swing.JLabel();
        jLabel217 = new javax.swing.JLabel();
        jPanel46 = new javax.swing.JPanel();
        jLabel231 = new javax.swing.JLabel();
        jLabel232 = new javax.swing.JLabel();
        jLabel233 = new javax.swing.JLabel();
        jTabbedPane5 = new javax.swing.JTabbedPane();
        jPanel47 = new javax.swing.JPanel();
        jTabbedPane6 = new javax.swing.JTabbedPane();
        jPanel36 = new javax.swing.JPanel();
        jLabel67 = new javax.swing.JLabel();
        jPanel37 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel258 = new javax.swing.JLabel();
        jPanel52 = new javax.swing.JPanel();
        jPanel48 = new javax.swing.JPanel();
        jLabel234 = new javax.swing.JLabel();
        jTextField28 = new javax.swing.JTextField();
        jTextField29 = new javax.swing.JTextField();
        jTextField30 = new javax.swing.JTextField();
        jTextField31 = new javax.swing.JTextField();
        jLabel235 = new javax.swing.JLabel();
        jLabel236 = new javax.swing.JLabel();
        jTextField32 = new javax.swing.JTextField();
        jTextField34 = new javax.swing.JTextField();
        jLabel238 = new javax.swing.JLabel();
        jLabel239 = new javax.swing.JLabel();
        jTextField35 = new javax.swing.JTextField();
        jLabel240 = new javax.swing.JLabel();
        jLabel241 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel242 = new javax.swing.JLabel();
        jLabel243 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel49 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel31 = new javax.swing.JPanel();
        jPanel51 = new javax.swing.JPanel();
        jLabel219 = new javax.swing.JLabel();
        jLabel230 = new javax.swing.JLabel();
        jLabel251 = new javax.swing.JLabel();
        jLabel252 = new javax.swing.JLabel();
        jLabel253 = new javax.swing.JLabel();
        jLabel254 = new javax.swing.JLabel();
        jPanel40 = new javax.swing.JPanel();
        jLabel183 = new javax.swing.JLabel();
        jLabel184 = new javax.swing.JLabel();
        jLabel185 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jTextField4 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jLabel112 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel73 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jLabel74 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel102 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        jLabel105 = new javax.swing.JLabel();
        jLabel106 = new javax.swing.JLabel();
        jLabel107 = new javax.swing.JLabel();
        jButton13 = new javax.swing.JButton();
        jTextField5 = new javax.swing.JTextField();
        jLabel108 = new javax.swing.JLabel();
        jLabel109 = new javax.swing.JLabel();
        jLabel110 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTable11 = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jButton15 = new javax.swing.JButton();
        jTextField6 = new javax.swing.JTextField();
        jLabel126 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jScrollPane9 = new javax.swing.JScrollPane();
        jTable7 = new javax.swing.JTable();
        jLabel26 = new javax.swing.JLabel();
        jLabel257 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel256 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel255 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jButton7 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jComboBox5 = new javax.swing.JComboBox();
        jLabel68 = new javax.swing.JLabel();
        jPanel58 = new javax.swing.JPanel();
        jLabel164 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MediCellar");
        setBackground(new java.awt.Color(0, 0, 0));
        setMinimumSize(new java.awt.Dimension(1000, 600));
        setPreferredSize(new java.awt.Dimension(900, 600));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable8.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "compo3", "c1", "c2", "c3"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane10.setViewportView(jTable8);
        if (jTable8.getColumnModel().getColumnCount() > 0) {
            jTable8.getColumnModel().getColumn(0).setResizable(false);
            jTable8.getColumnModel().getColumn(1).setResizable(false);
            jTable8.getColumnModel().getColumn(2).setResizable(false);
            jTable8.getColumnModel().getColumn(3).setResizable(false);
        }

        getContentPane().add(jScrollPane10, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 690, 80, 40));

        jTable9.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "compo2", "c1", "c2", "c3"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane11.setViewportView(jTable9);
        if (jTable9.getColumnModel().getColumnCount() > 0) {
            jTable9.getColumnModel().getColumn(0).setResizable(false);
            jTable9.getColumnModel().getColumn(1).setResizable(false);
        }

        getContentPane().add(jScrollPane11, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 690, 70, 50));

        jTable10.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "compo1", "c1", "c2", "c3"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane12.setViewportView(jTable10);
        if (jTable10.getColumnModel().getColumnCount() > 0) {
            jTable10.getColumnModel().getColumn(0).setResizable(false);
            jTable10.getColumnModel().getColumn(1).setResizable(false);
            jTable10.getColumnModel().getColumn(2).setResizable(false);
            jTable10.getColumnModel().getColumn(3).setResizable(false);
        }

        getContentPane().add(jScrollPane12, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 690, 50, 50));

        jTabbedPane2.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane2.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTabbedPane2.setOpaque(true);
        jTabbedPane2.setRequestFocusEnabled(false);

        jPanel16.setBackground(new java.awt.Color(255, 255, 255));

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(55, 77, 87));
        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/medistore/mc_120.png"))); // NOI18N

        jPanel21.setBackground(new java.awt.Color(255, 255, 255));
        jPanel21.setOpaque(false);

        jPanel17.setBackground(new java.awt.Color(57, 105, 138));
        jPanel17.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 0, true));
        jPanel17.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel17.setMinimumSize(new java.awt.Dimension(300, 300));
        jPanel17.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel30.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("Manage Sales");
        jPanel17.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 280, -1));

        jPanel18.setBackground(new java.awt.Color(55, 77, 87));
        jPanel18.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel18.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jPanel18.setMinimumSize(new java.awt.Dimension(300, 147));
        jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel29.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("Update Stock");
        jPanel18.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 280, -1));

        jPanel25.setBackground(new java.awt.Color(140, 56, 85));
        jPanel25.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel25.setMinimumSize(new java.awt.Dimension(70, 70));
        jPanel25.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel25MouseClicked(evt);
            }
        });

        jLabel172.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel172.setForeground(new java.awt.Color(255, 255, 255));
        jLabel172.setText("Settings");

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel25Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel172)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel25Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel172)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel24.setBackground(new java.awt.Color(255, 255, 255));
        jPanel24.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel24.setMinimumSize(new java.awt.Dimension(147, 147));
        jPanel24.setOpaque(false);

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 147, Short.MAX_VALUE)
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 147, Short.MAX_VALUE)
        );

        jPanel26.setBackground(new java.awt.Color(55, 77, 87));
        jPanel26.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel26.setMinimumSize(new java.awt.Dimension(70, 70));

        jLabel200.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel200.setForeground(new java.awt.Color(255, 255, 255));
        jLabel200.setText("Notes");

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel26Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel200)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel26Layout.createSequentialGroup()
                    .addGap(0, 27, Short.MAX_VALUE)
                    .addComponent(jLabel200)
                    .addGap(0, 27, Short.MAX_VALUE)))
        );

        jPanel23.setBackground(new java.awt.Color(57, 105, 138));
        jPanel23.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel23.setMinimumSize(new java.awt.Dimension(70, 70));

        jLabel57.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setText("Sales");

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel23Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel57)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
            .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel23Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jLabel57)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel19.setBackground(new java.awt.Color(140, 56, 85));
        jPanel19.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel19.setMinimumSize(new java.awt.Dimension(147, 147));
        jPanel19.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel37.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("Glance Over");
        jPanel19.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jLabel52.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setText("In-Stock");
        jPanel19.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 42, -1, -1));

        jLabel53.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("Out-of-Stock");
        jPanel19.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 86, -1, -1));

        jLabel54.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel54.setText("75489");
        jPanel19.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 280, -1));

        jLabel55.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(255, 255, 255));
        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel55.setText("75489");
        jPanel19.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 108, 280, -1));

        jPanel29.setBackground(new java.awt.Color(255, 255, 255));
        jPanel29.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel29.setOpaque(false);

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 146, Short.MAX_VALUE)
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 147, Short.MAX_VALUE)
        );

        jLayeredPane1.setBackground(new java.awt.Color(209, 52, 56));
        jLayeredPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLayeredPane1.setMinimumSize(new java.awt.Dimension(147, 147));
        jLayeredPane1.setOpaque(true);
        jLayeredPane1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel36.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("Search");
        jLayeredPane1.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 127, -1));

        jTextField8.setBackground(new java.awt.Color(209, 52, 56));
        jTextField8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField8.setForeground(new java.awt.Color(255, 255, 255));
        jTextField8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(190, 50, 50)));
        jTextField8.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jTextField8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });
        jLayeredPane1.add(jTextField8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 114, 127, -1));

        jLayeredPane2.setBackground(new java.awt.Color(55, 150, 198));
        jLayeredPane2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLayeredPane2.setOpaque(true);
        jLayeredPane2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLayeredPane2MouseClicked(evt);
            }
        });
        jLayeredPane2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField23.setBackground(new java.awt.Color(55, 150, 198));
        jTextField23.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField23.setForeground(new java.awt.Color(255, 255, 255));
        jTextField23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(100, 100, 255)));
        jTextField23.setMargin(new java.awt.Insets(2, 5, 2, 5));
        jTextField23.setOpaque(false);
        jTextField23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField23ActionPerformed(evt);
            }
        });
        jLayeredPane2.add(jTextField23, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 115, 127, -1));

        jLabel56.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setText("Shortlist");
        jLayeredPane2.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 130, -1));

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel21Layout.createSequentialGroup()
                                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLayeredPane2)))
                .addContainerGap(57, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(151, 151, 151))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel17, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel21Layout.createSequentialGroup()
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jPanel26, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel25, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(7, 7, 7)
                                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel23, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel29, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel21Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLayeredPane2))))))
                .addGap(18, 18, 18)
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel17.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane2.setSelectedIndex(2);
                }
                else{
                    registerMsg();
                }
            }
        });
        jPanel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane2.setSelectedIndex(1);
                }
                else {
                    registerMsg();
                }
            }
        });
        jPanel26.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane3.setSelectedIndex(3);
                    jTabbedPane2.setSelectedIndex(3);
                }else{
                    registerMsg();
                }
            }
        });
        jPanel23.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane3.setSelectedIndex(0);
                    jTabbedPane2.setSelectedIndex(3);
                }else{
                    registerMsg();
                }
            }
        });
        jPanel19.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane1.setSelectedIndex(1);
                    jTabbedPane2.setSelectedIndex(5);
                }else{
                    registerMsg();
                }
            }
        });
        jLayeredPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane1.setSelectedIndex(0);
                    jTabbedPane2.setSelectedIndex(5);
                }else{
                    registerMsg();
                }
            }
        });

        jPanel50.setBackground(new java.awt.Color(250, 250, 250));
        jPanel50.setForeground(new java.awt.Color(55, 150, 198));
        jPanel50.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel50.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel50MouseClicked(evt);
            }
        });

        jLabel237.setFont(new java.awt.Font("Microsoft Yi Baiti", 1, 36)); // NOI18N
        jLabel237.setForeground(new java.awt.Color(55, 150, 198));
        jLabel237.setText("Register your product!!!");
        jLabel237.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel237.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel237MouseClicked(evt);
            }
        });

        jLabel244.setFont(new java.awt.Font("Microsoft Yi Baiti", 1, 18)); // NOI18N
        jLabel244.setForeground(new java.awt.Color(55, 150, 198));
        jLabel244.setText("Don't have a product key?");
        jLabel244.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel244.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel244.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel244MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel50Layout = new javax.swing.GroupLayout(jPanel50);
        jPanel50.setLayout(jPanel50Layout);
        jPanel50Layout.setHorizontalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel50Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel244)
                .addGap(68, 68, 68))
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel237)
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel50Layout.setVerticalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel237)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(jLabel244)
                .addContainerGap())
        );

        jPanel53.setBackground(new java.awt.Color(55, 150, 198));

        javax.swing.GroupLayout jPanel53Layout = new javax.swing.GroupLayout(jPanel53);
        jPanel53.setLayout(jPanel53Layout);
        jPanel53Layout.setHorizontalGroup(
            jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel53Layout.setVerticalGroup(
            jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 48, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 86, Short.MAX_VALUE))
            .addComponent(jPanel53, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jPanel50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)))
                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jPanel53, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        jTabbedPane2.addTab("tab4", jPanel16);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(57, 105, 138), 2, true));

        jLabel28.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel28.setText("Description:");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel31.setText("Note:");

        jTextField9.setEnabled(false);
        jTextField9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField9ActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel32.setText("Block");

        jLabel33.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel33.setText("Name:");

        jLabel34.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel34.setText("Compositions");

        jLabel35.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel35.setText("Quantity:");

        jTextField10.setEnabled(false);
        jTextField10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField10ActionPerformed(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel38.setText("pcs");

        jTextField11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField11ActionPerformed(evt);
            }
        });

        jTextField12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField12ActionPerformed(evt);
            }
        });

        jTextField13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField13ActionPerformed(evt);
            }
        });

        jTextField14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField14ActionPerformed(evt);
            }
        });

        jLabel40.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel40.setText("Stock location:");

        jLabel3.setBackground(new java.awt.Color(57, 105, 138));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("ADD NEW MEDICINE DETAILS");
        jLabel3.setOpaque(true);

        jLabel45.setText("MRP:");

        jLabel46.setText("/unit");

        jTextField20.setDisabledTextColor(new java.awt.Color(140, 140, 140));
        jTextField20.setEnabled(false);
        jTextField20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField20ActionPerformed(evt);
            }
        });

        jCheckBox1.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBox1.setText("Quick Add to Stock");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jScrollPane14.setViewportView(jEditorPane1);

        jScrollPane15.setViewportView(jEditorPane2);

        jLabel49.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel49.setText("1.");

        jLabel50.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel50.setText("2.");

        jLabel51.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel51.setText("3.");

        jLabel58.setBackground(new java.awt.Color(57, 105, 138));
        jLabel58.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(255, 255, 255));
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setText("ADD DETAILS");
        jLabel58.setOpaque(true);
        jLabel58.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel58MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel34)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel35)
                                .addGap(11, 11, 11)
                                .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel38)
                                .addGap(37, 37, 37)
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel46)))
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel28))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel31)
                                    .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel49)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel51)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel50)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel33)
                .addGap(20, 20, 20)
                .addComponent(jTextField14)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jCheckBox1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel40)
                        .addGap(22, 22, 22)
                        .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel32)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel33))
                    .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(jCheckBox1)
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38)
                    .addComponent(jLabel35)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel45)
                    .addComponent(jLabel46))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40)
                    .addComponent(jLabel32))
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel34)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel49))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel50))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel51))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel28))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        /*jCheckBox1.addItemListener(new ItemListener(){

            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    jTextField9.setEnabled(true);
                    jTextField10.setEnabled(true);
                    jTextField20.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                    jTextField9.setEnabled(false);
                    jTextField10.setEnabled(false);
                    jTextField20.setEnabled(false);
                }

            }
        });
        */

        jPanel15.setBackground(new java.awt.Color(255, 255, 255));
        jPanel15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(57, 105, 138), 2, true));

        jLabel2.setBackground(new java.awt.Color(57, 105, 138));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("UPDATE STOCK");
        jLabel2.setOpaque(true);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Medicine Name", "Quantity before adding", "Now Added", "Total Available"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setGridColor(new java.awt.Color(255, 255, 255));
        jTable1.setName(""); // NOI18N
        jTable1.setOpaque(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.setBackground(new Color(255, 255, 255));
        jTable1.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane1.setViewportView(jTable1);

        jLabel39.setBackground(new java.awt.Color(57, 105, 138));
        jLabel39.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setText("Show full stock");
        jLabel39.setOpaque(true);
        jLabel39.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel39MouseClicked(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(255, 255, 255));
        jButton8.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jButton8.setForeground(new java.awt.Color(57, 105, 138));
        jButton8.setText("Add to list");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel123.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel123.setText("Currently added:");

        jLabel41.setText("Quantity (pcs):");

        jLabel43.setText("MRP (/unit):");

        jLabel47.setText("Location (Block):");

        jTextField16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField16ActionPerformed(evt);
            }
        });

        jTextField19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField19ActionPerformed(evt);
            }
        });

        jTextField22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField22ActionPerformed(evt);
            }
        });

        jTextField15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField15ActionPerformed(evt);
            }
        });

        jLabel124.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel124.setText("Name:");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel39, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel124))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel41)
                            .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel43))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addComponent(jLabel47)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel123))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel124)
                    .addComponent(jLabel41)
                    .addComponent(jLabel43)
                    .addComponent(jLabel47))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel123)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel28.setBackground(new java.awt.Color(0, 0, 0));

        jLabel72.setBackground(new java.awt.Color(57, 105, 138));
        jLabel72.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel72.setForeground(new java.awt.Color(255, 255, 255));
        jLabel72.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel72.setText("Home");
        jLabel72.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel72.setOpaque(true);

        jLabel78.setBackground(new java.awt.Color(57, 105, 138));
        jLabel78.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel78.setForeground(new java.awt.Color(255, 255, 255));
        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel78.setText("Sales");
        jLabel78.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel78.setOpaque(true);

        jLabel117.setBackground(new java.awt.Color(255, 255, 255));
        jLabel117.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel117.setForeground(new java.awt.Color(57, 105, 138));
        jLabel117.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel117.setText("Update Stock");
        jLabel117.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel117.setOpaque(true);

        jLabel118.setBackground(new java.awt.Color(57, 105, 138));
        jLabel118.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel118.setForeground(new java.awt.Color(255, 255, 255));
        jLabel118.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel118.setText("Search & Glance");
        jLabel118.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel118.setOpaque(true);

        jLabel119.setBackground(new java.awt.Color(57, 105, 138));
        jLabel119.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel119.setForeground(new java.awt.Color(255, 255, 255));
        jLabel119.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel119.setText("Others");
        jLabel119.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel119.setOpaque(true);

        jLabel120.setBackground(new java.awt.Color(57, 105, 138));
        jLabel120.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel120.setForeground(new java.awt.Color(255, 255, 255));
        jLabel120.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel120.setText("Settings");
        jLabel120.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel120.setOpaque(true);

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addComponent(jLabel72, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel117, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel118, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel119, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel120, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel72, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel117, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel118, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel119, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel120, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel72.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
                refresh_jTable2();
                refresh_jTable3();
            }
        });
        jLabel78.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(2);
            }
        });
        jLabel118.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(5);
            }
        });
        jLabel119.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(3);
            }
        });
        jLabel120.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(117, 117, 117))
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 452, Short.MAX_VALUE))
                .addContainerGap(96, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("UPDATE", jPanel9);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        jTextField17.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jTextField17.setToolTipText("Enter the name of medicine that you want to search.");
        jTextField17.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField17FocusGained(evt);
            }
        });
        jTextField17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField17ActionPerformed(evt);
            }
        });

        jLabel90.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel90.setText("Name:");

        jButton20.setBackground(new java.awt.Color(255, 255, 255));
        jButton20.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jButton20.setForeground(new java.awt.Color(55, 77, 87));
        jButton20.setText("Search");
        jButton20.setToolTipText("Click to search the details about the medicine");
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(55, 77, 87), 2, true));

        jButton21.setBackground(new java.awt.Color(255, 255, 255));
        jButton21.setForeground(new java.awt.Color(55, 77, 87));
        jButton21.setText("Sell");
        jButton21.setToolTipText("Sell the shortlisted medicines");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jButton22.setBackground(new java.awt.Color(255, 255, 255));
        jButton22.setForeground(new java.awt.Color(55, 77, 87));
        jButton22.setText("Cancel");
        jButton22.setToolTipText("Cancel the shortlisted medicines");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        jLabel98.setBackground(new java.awt.Color(55, 77, 87));
        jLabel98.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel98.setForeground(new java.awt.Color(255, 255, 255));
        jLabel98.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel98.setText("SHORTLISTED ITEMS");
        jLabel98.setOpaque(true);

        jTable12.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Quantity", "Rate", "Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable12.setToolTipText("Shortlisted medicines to sell in bulk");
        jTable12.setOpaque(true);
        jTable12.setFillsViewportHeight(true);
        jTable12.setBackground(new Color(255, 255, 255));
        jTable12.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane19.setViewportView(jTable12);
        if (jTable12.getColumnModel().getColumnCount() > 0) {
            jTable12.getColumnModel().getColumn(0).setResizable(false);
            jTable12.getColumnModel().getColumn(1).setResizable(false);
            jTable12.getColumnModel().getColumn(2).setResizable(false);
            jTable12.getColumnModel().getColumn(3).setResizable(false);
        }

        jLabel84.setText("Payable Amount:");

        jLabel116.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel116.setForeground(new java.awt.Color(55, 77, 87));
        jLabel116.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel116.setText("0");

        jLabel96.setText("only");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jButton21)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton22))
                        .addGroup(jPanel8Layout.createSequentialGroup()
                            .addComponent(jLabel84)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel116, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel96))))
                .addGap(0, 9, Short.MAX_VALUE))
            .addComponent(jLabel98, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel98, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel116)
                    .addComponent(jLabel84)
                    .addComponent(jLabel96))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton21)
                    .addComponent(jButton22))
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(55, 77, 87), 2, true));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField21.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField21.setToolTipText("Enter the quantity ");
        jTextField21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField21ActionPerformed(evt);
            }
        });
        jTextField21.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField21KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField21KeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField21KeyTyped(evt);
            }
        });
        jPanel10.add(jTextField21, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 143, -1));

        jTextField18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField18.setToolTipText("Leave this place vacant, if you want to sell the medicine at saved price");
        jTextField18.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField18KeyReleased(evt);
            }
        });
        jPanel10.add(jTextField18, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 74, 143, -1));

        jLabel101.setText("Quantity to be sold:");
        jPanel10.add(jLabel101, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 38, -1, -1));

        jLabel95.setText("At Price:");
        jPanel10.add(jLabel95, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 82, -1, -1));

        jLabel85.setText("/unit");
        jPanel10.add(jLabel85, new org.netbeans.lib.awtextra.AbsoluteConstraints(259, 82, -1, -1));

        jLabel83.setText("Pcs");
        jPanel10.add(jLabel83, new org.netbeans.lib.awtextra.AbsoluteConstraints(265, 38, -1, -1));

        jLabel87.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jLabel87.setForeground(new java.awt.Color(55, 77, 87));
        jLabel87.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel87.setText("0.0");
        jLabel87.setToolTipText("Amount to be paid by customer");
        jPanel10.add(jLabel87, new org.netbeans.lib.awtextra.AbsoluteConstraints(45, 154, 214, 49));
        jLabel87.getAccessibleContext().setAccessibleName("99984.00");

        jLabel88.setText("only");
        jPanel10.add(jLabel88, new org.netbeans.lib.awtextra.AbsoluteConstraints(265, 187, -1, -1));

        jLabel81.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel81.setForeground(new java.awt.Color(55, 77, 87));
        jLabel81.setText("Rs.");
        jPanel10.add(jLabel81, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 172, -1, -1));

        jLabel20.setText("Payable Amount:");
        jPanel10.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 134, -1, -1));

        jButton18.setBackground(new java.awt.Color(255, 255, 255));
        jButton18.setForeground(new java.awt.Color(55, 77, 87));
        jButton18.setText("Sell Now");
        jButton18.setToolTipText("Click to sell it immidiately");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton18, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 253, -1, -1));

        jButton19.setBackground(new java.awt.Color(255, 255, 255));
        jButton19.setForeground(new java.awt.Color(55, 77, 87));
        jButton19.setText("Short list to sell");
        jButton19.setToolTipText("Shortlist to sell it later in bulk (Ideal for customers demanding varaties of medicines)");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton19, new org.netbeans.lib.awtextra.AbsoluteConstraints(89, 253, -1, -1));

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(55, 77, 87), 2, true));

        jLabel86.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jLabel86.setForeground(new java.awt.Color(55, 77, 87));
        jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel86.setText("0.0");
        jLabel86.setToolTipText("Price of medicine in INR");

        jLabel113.setText("Pcs");

        jLabel114.setText("Price:");

        jLabel82.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jLabel82.setForeground(new java.awt.Color(55, 77, 87));
        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel82.setText("0");
        jLabel82.setToolTipText("Available medicine stock");

        jLabel115.setText("/unit");

        jLabel91.setText("Available in stock: ");

        jLabel89.setText("Compositions:");

        jLabel92.setText("Stock location:");

        jLabel93.setLabelFor(jEditorPane4);
        jLabel93.setText("Description:");

        jLabel94.setText("Note:");

        jEditorPane3.setEditable(false);
        jEditorPane3.setBorder(null);
        jEditorPane3.setToolTipText("Medicine composition are shown here");
        jEditorPane3.setFocusable(false);
        jScrollPane16.setViewportView(jEditorPane3);

        jEditorPane4.setEditable(false);
        jEditorPane4.setBorder(null);
        jEditorPane4.setToolTipText("saved description about the medicine");
        jEditorPane4.setFocusable(false);
        jScrollPane17.setViewportView(jEditorPane4);

        jEditorPane5.setEditable(false);
        jEditorPane5.setBorder(null);
        jEditorPane5.setToolTipText("Saved note about this medicine");
        jEditorPane5.setFocusable(false);
        jScrollPane18.setViewportView(jEditorPane5);

        jLabel173.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jLabel173.setForeground(new java.awt.Color(55, 77, 87));
        jLabel173.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel173.setText("NA");
        jLabel173.setToolTipText("stored location of the medicine");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel91)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel82, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel113))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel94)
                                    .addComponent(jLabel89))
                                .addGap(7, 7, 7)
                                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel12Layout.createSequentialGroup()
                                        .addGap(68, 68, 68)
                                        .addComponent(jLabel93))
                                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel114)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel86, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel115))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel92)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel173, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel113)
                        .addGap(5, 5, 5))
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel82, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel91)))
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel114)
                        .addComponent(jLabel86, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel115)))
                .addGap(20, 20, 20)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel92)
                    .addComponent(jLabel173, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel89)
                        .addGap(45, 45, 45)
                        .addComponent(jLabel94))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel93)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(jScrollPane18))
                .addContainerGap())
        );

        jPanel22.setBackground(new java.awt.Color(0, 0, 0));

        jLabel62.setBackground(new java.awt.Color(55, 77, 87));
        jLabel62.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(255, 255, 255));
        jLabel62.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel62.setText("Home");
        jLabel62.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel62.setOpaque(true);

        jLabel61.setBackground(new java.awt.Color(255, 255, 255));
        jLabel61.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(55, 77, 87));
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel61.setText("Sales");
        jLabel61.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel61.setOpaque(true);

        jLabel60.setBackground(new java.awt.Color(55, 77, 87));
        jLabel60.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel60.setForeground(new java.awt.Color(255, 255, 255));
        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel60.setText("Update Stock");
        jLabel60.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel60.setOpaque(true);

        jLabel63.setBackground(new java.awt.Color(55, 77, 87));
        jLabel63.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel63.setForeground(new java.awt.Color(255, 255, 255));
        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel63.setText("Search & Glance");
        jLabel63.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel63.setOpaque(true);

        jLabel65.setBackground(new java.awt.Color(55, 77, 87));
        jLabel65.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel65.setForeground(new java.awt.Color(255, 255, 255));
        jLabel65.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel65.setText("Settings");
        jLabel65.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel65.setOpaque(true);

        jLabel66.setBackground(new java.awt.Color(55, 77, 87));
        jLabel66.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel66.setForeground(new java.awt.Color(255, 255, 255));
        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel66.setText("Others");
        jLabel66.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel66.setOpaque(true);
        jLabel66.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel66MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel63, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel65, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel62, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel63, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel65, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel66, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
                refresh_jTable2();
                refresh_jTable3();

            }
        });
        jLabel60.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(1);
            }
        });
        jLabel63.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(5);
            }
        });
        jLabel65.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jLabel65.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 255, 0));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(189, 189, 189)
                        .addComponent(jLabel90)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, 497, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton20))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton20))
                    .addComponent(jLabel90, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("SALES", jPanel7);

        jPanel41.setBackground(new java.awt.Color(255, 255, 255));

        jPanel30.setBackground(new java.awt.Color(0, 0, 0));

        jLabel97.setBackground(new java.awt.Color(209, 52, 56));
        jLabel97.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel97.setForeground(new java.awt.Color(255, 255, 255));
        jLabel97.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel97.setText("Home");
        jLabel97.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel97.setOpaque(true);
        jLabel97.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel97MouseClicked(evt);
            }
        });

        jLabel99.setBackground(new java.awt.Color(209, 52, 56));
        jLabel99.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel99.setForeground(new java.awt.Color(255, 255, 255));
        jLabel99.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel99.setText("Sales");
        jLabel99.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel99.setOpaque(true);
        jLabel99.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel99MouseClicked(evt);
            }
        });

        jLabel100.setBackground(new java.awt.Color(209, 52, 56));
        jLabel100.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel100.setForeground(new java.awt.Color(255, 255, 255));
        jLabel100.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel100.setText("Update Stock");
        jLabel100.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel100.setOpaque(true);
        jLabel100.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel100MouseClicked(evt);
            }
        });

        jLabel121.setBackground(new java.awt.Color(209, 52, 56));
        jLabel121.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel121.setForeground(new java.awt.Color(255, 255, 255));
        jLabel121.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel121.setText("Search & Glance");
        jLabel121.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel121.setOpaque(true);
        jLabel121.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel121MouseClicked(evt);
            }
        });

        jLabel122.setBackground(new java.awt.Color(255, 255, 255));
        jLabel122.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel122.setForeground(new java.awt.Color(209, 52, 56));
        jLabel122.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel122.setText("Others");
        jLabel122.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel122.setOpaque(true);

        jLabel125.setBackground(new java.awt.Color(209, 52, 56));
        jLabel125.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel125.setForeground(new java.awt.Color(255, 255, 255));
        jLabel125.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel125.setText("Settings");
        jLabel125.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel125.setOpaque(true);
        jLabel125.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel125MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addComponent(jLabel97, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel99, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel100, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel121, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel122, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel125, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel97, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel100, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel121, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel99, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel122, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel125, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel128.setBackground(new java.awt.Color(209, 52, 56));
        jLabel128.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel128.setForeground(new java.awt.Color(255, 255, 255));
        jLabel128.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel128.setText("ShortListed Items");
        jLabel128.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel128.setOpaque(true);
        jLabel128.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel128MouseClicked(evt);
            }
        });

        jLabel129.setBackground(new java.awt.Color(209, 52, 56));
        jLabel129.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel129.setForeground(new java.awt.Color(255, 255, 255));
        jLabel129.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel129.setText("Sales Details");
        jLabel129.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel129.setOpaque(true);
        jLabel129.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel129MouseClicked(evt);
            }
        });

        jLabel130.setBackground(new java.awt.Color(209, 52, 56));
        jLabel130.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel130.setForeground(new java.awt.Color(255, 255, 255));
        jLabel130.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel130.setText("Saved Note");
        jLabel130.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel130.setOpaque(true);
        jLabel130.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel130MouseClicked(evt);
            }
        });

        jTabbedPane3.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

        jPanel32.setBackground(new java.awt.Color(255, 255, 255));

        jLabel131.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel131.setForeground(new java.awt.Color(209, 52, 56));
        jLabel131.setText("Today's Sales detail");

        jLabel132.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel132.setForeground(new java.awt.Color(209, 52, 56));
        jLabel132.setText("Lastday Sales detail");

        jLabel133.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel133.setForeground(new java.awt.Color(209, 52, 56));
        jLabel133.setText("Medicines Sold:");

        jLabel135.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel135.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel135.setText("2000");

        jLabel136.setText("pcs");

        jLabel137.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel137.setForeground(new java.awt.Color(209, 52, 56));
        jLabel137.setText("Amount:");

        jLabel138.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel138.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel138.setText("90008899.0");

        jLabel139.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel139.setText("/-");

        jLabel140.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel140.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel140.setText("Rs.");

        jLabel141.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel141.setForeground(new java.awt.Color(209, 52, 56));
        jLabel141.setText("Date:");

        jLabel142.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel142.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel142.setText("24/12/2015");

        jTable13.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Rate", "Quantity", "Amount", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable13.setOpaque(true);
        jTable13.setFillsViewportHeight(true);
        jTable13.setBackground(new Color(255, 255, 255));
        jTable13.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane20.setViewportView(jTable13);
        refresh_jTable13();

        jLabel143.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel143.setForeground(new java.awt.Color(209, 52, 56));
        jLabel143.setText("Medicines Sold:");

        jLabel144.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel144.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel144.setText("2000");

        jLabel145.setText("pcs");

        jLabel146.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel146.setForeground(new java.awt.Color(209, 52, 56));
        jLabel146.setText("Amount:");

        jLabel147.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel147.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel147.setText("Rs.");

        jLabel148.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel148.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel148.setText("90008899.0");

        jLabel149.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel149.setText("/-");

        jTable17.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Rate", "Quantity", "Amount", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable17.setOpaque(true);
        jTable17.setFillsViewportHeight(true);
        jTable17.setBackground(new Color(255, 255, 255));
        jTable17.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane26.setViewportView(jTable17);
        refresh_jTable17();

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel132)
                    .addGroup(jPanel32Layout.createSequentialGroup()
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel32Layout.createSequentialGroup()
                                .addComponent(jLabel137)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel140)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel138, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel32Layout.createSequentialGroup()
                                .addComponent(jLabel133)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel135, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel32Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel139))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel32Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel136))))
                    .addGroup(jPanel32Layout.createSequentialGroup()
                        .addComponent(jLabel141)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel142, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(63, 63, 63)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel131)
                    .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel32Layout.createSequentialGroup()
                            .addComponent(jLabel146)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel147)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel148, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel149))
                        .addGroup(jPanel32Layout.createSequentialGroup()
                            .addComponent(jLabel143)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel144, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel145))
                        .addComponent(jScrollPane26, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel132)
                    .addComponent(jLabel131))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel32Layout.createSequentialGroup()
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel141)
                            .addComponent(jLabel142))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel133)
                            .addComponent(jLabel135)
                            .addComponent(jLabel136))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel138)
                            .addComponent(jLabel137)
                            .addComponent(jLabel139)
                            .addComponent(jLabel140))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel32Layout.createSequentialGroup()
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel143)
                            .addComponent(jLabel144)
                            .addComponent(jLabel145))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel148)
                            .addComponent(jLabel146)
                            .addComponent(jLabel149)
                            .addComponent(jLabel147))
                        .addGap(31, 31, 31)))
                .addGroup(jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane20, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                    .addComponent(jScrollPane26, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel138.getAccessibleContext().setAccessibleName("2000.0");

        jTabbedPane3.addTab("1", jPanel32);

        jPanel33.setBackground(new java.awt.Color(255, 255, 255));

        jLabel150.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel150.setForeground(new java.awt.Color(209, 52, 56));
        jLabel150.setText("Sales Record");

        jLabel151.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel151.setForeground(new java.awt.Color(209, 52, 56));
        jLabel151.setText("Search by Date:");

        jLabel152.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel152.setForeground(new java.awt.Color(209, 52, 56));
        jLabel152.setText("Search by Month:");

        jLabel153.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel153.setForeground(new java.awt.Color(209, 52, 56));
        jLabel153.setText("Search by Year:");

        jXDatePicker1.setBackground(new java.awt.Color(255, 255, 255));
        jXDatePicker1.setForeground(new java.awt.Color(205, 52, 56));
        jXDatePicker1.setFocusable(false);
        jXDatePicker1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXDatePicker1ActionPerformed(evt);
            }
        });

        jLabel154.setBackground(new java.awt.Color(209, 52, 56));
        jLabel154.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel154.setForeground(new java.awt.Color(255, 255, 255));
        jLabel154.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel154.setText("Search");
        jLabel154.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel154.setOpaque(true);
        jLabel154.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel154MouseClicked(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        jLabel155.setBackground(new java.awt.Color(209, 52, 56));
        jLabel155.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel155.setForeground(new java.awt.Color(255, 255, 255));
        jLabel155.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel155.setText("Search");
        jLabel155.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel155.setOpaque(true);
        jLabel155.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel155MouseClicked(evt);
            }
        });

        jLabel156.setBackground(new java.awt.Color(209, 52, 56));
        jLabel156.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel156.setForeground(new java.awt.Color(255, 255, 255));
        jLabel156.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel156.setText("Search");
        jLabel156.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel156.setOpaque(true);
        jLabel156.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel156MouseClicked(evt);
            }
        });

        jTable15.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Quantity", "Price", "Time", "Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable15.setOpaque(true);
        jTable15.setFillsViewportHeight(true);
        jTable15.setBackground(new Color(255, 255, 255));
        jTable15.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane22.setViewportView(jTable15);
        if (jTable15.getColumnModel().getColumnCount() > 0) {
            jTable15.getColumnModel().getColumn(0).setResizable(false);
            jTable15.getColumnModel().getColumn(1).setResizable(false);
            jTable15.getColumnModel().getColumn(2).setResizable(false);
            jTable15.getColumnModel().getColumn(3).setResizable(false);
            jTable15.getColumnModel().getColumn(4).setResizable(false);
        }

        jLabel157.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel157.setForeground(new java.awt.Color(209, 52, 56));
        jLabel157.setText("Medicines Sold:");

        jLabel158.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel158.setForeground(new java.awt.Color(209, 52, 56));
        jLabel158.setText("Amount:");

        jLabel159.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel159.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel159.setText("Rs.");

        jLabel180.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel180.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel180.setText("0");

        jLabel181.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel181.setText("/-");

        jLabel182.setText("pcs");

        jLabel186.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel186.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel186.setText("0");

        jTable18.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Quantity", "Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable18.setOpaque(true);
        jTable18.setFillsViewportHeight(true);
        jTable18.setBackground(new Color(255, 255, 255));
        jTable18.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane27.setViewportView(jTable18);
        if (jTable18.getColumnModel().getColumnCount() > 0) {
            jTable18.getColumnModel().getColumn(0).setResizable(false);
            jTable18.getColumnModel().getColumn(1).setResizable(false);
            jTable18.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel201.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel201.setForeground(new java.awt.Color(209, 52, 56));
        jLabel201.setText("Each unit details:");

        jLabel202.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel202.setForeground(new java.awt.Color(209, 52, 56));
        jLabel202.setText("Each Item details:");

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addComponent(jLabel157)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel186, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel150)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addComponent(jXDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel154, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel151))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel33Layout.createSequentialGroup()
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel152))
                                .addGap(10, 10, 10)
                                .addComponent(jLabel155, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel33Layout.createSequentialGroup()
                                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel156, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel153, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addComponent(jLabel182)
                                .addGap(105, 105, 105)
                                .addComponent(jLabel158)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel159)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel180, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel181))))
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel201))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addComponent(jLabel202)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addComponent(jScrollPane27, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(57, Short.MAX_VALUE))))))
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel33Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel150)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jXDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel154, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel155, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel152)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel151)
                                    .addComponent(jLabel153, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel156, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(18, 18, 18)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel157)
                    .addComponent(jLabel186)
                    .addComponent(jLabel182)
                    .addComponent(jLabel180)
                    .addComponent(jLabel158)
                    .addComponent(jLabel181)
                    .addComponent(jLabel159))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel201)
                    .addComponent(jLabel202))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane27, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });

        jTabbedPane3.addTab("2", jPanel33);

        jPanel35.setBackground(new java.awt.Color(255, 255, 255));

        jTable16.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Adding Date", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable16.setOpaque(true);
        jTable16.setFillsViewportHeight(true);
        jTable16.setBackground(new Color(255, 255, 255));
        jTable16.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane23.setViewportView(jTable16);
        if (jTable16.getColumnModel().getColumnCount() > 0) {
            jTable16.getColumnModel().getColumn(0).setResizable(false);
            jTable16.getColumnModel().getColumn(1).setResizable(false);
            jTable16.getColumnModel().getColumn(2).setResizable(false);
        }
        refresh_jTable16();

        jLabel187.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel187.setForeground(new java.awt.Color(209, 52, 56));
        jLabel187.setText("Shortlisted Items:");

        jLabel188.setBackground(new java.awt.Color(209, 52, 56));
        jLabel188.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel188.setForeground(new java.awt.Color(255, 255, 255));
        jLabel188.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel188.setText("Enlist");
        jLabel188.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel188.setOpaque(true);
        jLabel188.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel188MouseClicked(evt);
            }
        });

        jLabel189.setBackground(new java.awt.Color(209, 52, 56));
        jLabel189.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel189.setForeground(new java.awt.Color(255, 255, 255));
        jLabel189.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel189.setText("Delist");
        jLabel189.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel189.setOpaque(true);
        jLabel189.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel189MouseClicked(evt);
            }
        });

        jTextField24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField24ActionPerformed(evt);
            }
        });

        jLabel199.setBackground(new java.awt.Color(209, 52, 56));
        jLabel199.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel199.setForeground(new java.awt.Color(255, 255, 255));
        jLabel199.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel199.setText("Clear List");
        jLabel199.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel199.setOpaque(true);
        jLabel199.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel199MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel35Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 707, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel35Layout.createSequentialGroup()
                            .addComponent(jLabel199, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel188, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel189, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel187))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel35Layout.setVerticalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel35Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel187)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel199, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel188, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel189, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });

        jTabbedPane3.addTab("3", jPanel35);

        jPanel34.setBackground(new java.awt.Color(255, 255, 255));

        jLabel190.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel190.setForeground(new java.awt.Color(209, 52, 56));
        jLabel190.setText("Saved Notes:");

        jLabel191.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel191.setForeground(new java.awt.Color(209, 52, 56));
        jLabel191.setText("Note List:");

        jComboBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox4ItemStateChanged(evt);
            }
        });
        jComboBox4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jComboBox4KeyReleased(evt);
            }
        });

        jScrollPane24.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane24.setViewportView(jEditorPane6);

        jLabel192.setBackground(new java.awt.Color(209, 52, 56));
        jLabel192.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel192.setForeground(new java.awt.Color(255, 255, 255));
        jLabel192.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel192.setText("Delete");
        jLabel192.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel192.setOpaque(true);
        jLabel192.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel192MouseClicked(evt);
            }
        });

        jLabel193.setBackground(new java.awt.Color(209, 52, 56));
        jLabel193.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel193.setForeground(new java.awt.Color(255, 255, 255));
        jLabel193.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel193.setText("Clear");
        jLabel193.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel193.setOpaque(true);
        jLabel193.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel193MouseClicked(evt);
            }
        });

        jLabel194.setBackground(new java.awt.Color(209, 52, 56));
        jLabel194.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel194.setForeground(new java.awt.Color(255, 255, 255));
        jLabel194.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel194.setText("Update");
        jLabel194.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel194.setOpaque(true);
        jLabel194.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel194MouseClicked(evt);
            }
        });

        jLabel195.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel195.setForeground(new java.awt.Color(209, 52, 56));
        jLabel195.setText("Name:");

        jLabel196.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        jLabel197.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel197.setForeground(new java.awt.Color(209, 52, 56));
        jLabel197.setText("Date");

        jLabel198.setBackground(new java.awt.Color(209, 52, 56));
        jLabel198.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel198.setForeground(new java.awt.Color(255, 255, 255));
        jLabel198.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel198.setText("Add New");
        jLabel198.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel198.setOpaque(true);
        jLabel198.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel198MouseClicked(evt);
            }
        });

        jTextField25.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextField25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField25ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel190)
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addComponent(jLabel191)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel34Layout.createSequentialGroup()
                            .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel195)
                                .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(27, 27, 27)
                            .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel197)
                                .addComponent(jLabel196, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(jLabel198, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(9, 9, 9)
                            .addComponent(jLabel194, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(9, 9, 9)
                            .addComponent(jLabel193, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(9, 9, 9)
                            .addComponent(jLabel192, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 760, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 55, Short.MAX_VALUE))
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel190)
                .addGap(12, 12, 12)
                .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel34Layout.createSequentialGroup()
                        .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel191)
                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel195)
                            .addComponent(jLabel197))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel196, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel198, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel194, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel193, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel192, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        updateNote();
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });

        jTabbedPane3.addTab("4", jPanel34);

        jLabel134.setBackground(new java.awt.Color(209, 52, 56));
        jLabel134.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel134.setForeground(new java.awt.Color(255, 255, 255));
        jLabel134.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel134.setText("Sales Record");
        jLabel134.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel134.setOpaque(true);
        jLabel134.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel134MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 17, Short.MAX_VALUE))
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel130, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel129, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel128, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel134, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 862, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel41Layout.setVerticalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel41Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jLabel129, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel134, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel128, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel130, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel62.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });

        jTabbedPane2.addTab("others", jPanel41);

        jPanel42.setBackground(new java.awt.Color(255, 255, 255));

        jPanel39.setBackground(new java.awt.Color(0, 0, 0));

        jLabel203.setBackground(new java.awt.Color(55, 150, 198));
        jLabel203.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel203.setForeground(new java.awt.Color(255, 255, 255));
        jLabel203.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel203.setText("Home");
        jLabel203.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel203.setOpaque(true);
        jLabel203.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel203MouseClicked(evt);
            }
        });

        jLabel204.setBackground(new java.awt.Color(55, 150, 198));
        jLabel204.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel204.setForeground(new java.awt.Color(255, 255, 255));
        jLabel204.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel204.setText("Sales");
        jLabel204.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel204.setOpaque(true);

        jLabel205.setBackground(new java.awt.Color(55, 150, 198));
        jLabel205.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel205.setForeground(new java.awt.Color(255, 255, 255));
        jLabel205.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel205.setText("Update Stock");
        jLabel205.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel205.setOpaque(true);

        jLabel206.setBackground(new java.awt.Color(55, 150, 198));
        jLabel206.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel206.setForeground(new java.awt.Color(255, 255, 255));
        jLabel206.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel206.setText("Search & Glance");
        jLabel206.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel206.setOpaque(true);

        jLabel207.setBackground(new java.awt.Color(55, 150, 198));
        jLabel207.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel207.setForeground(new java.awt.Color(255, 255, 255));
        jLabel207.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel207.setText("Others");
        jLabel207.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel207.setOpaque(true);
        jLabel207.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel207MouseClicked(evt);
            }
        });

        jLabel208.setBackground(new java.awt.Color(255, 255, 255));
        jLabel208.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel208.setForeground(new java.awt.Color(55, 150, 198));
        jLabel208.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel208.setText("Settings");
        jLabel208.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel208.setOpaque(true);

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addComponent(jLabel203, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel204, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel205, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel206, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel207, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel208, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel203, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel205, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel206, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel204, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel207, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel208, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel203.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
            }
        });
        jLabel204.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane2.setSelectedIndex(2);
                }else{
                    registerMsg();
                }
            }
        });
        jLabel205.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane2.setSelectedIndex(1);
                }else{
                    registerMsg();
                }
            }
        });
        jLabel206.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if(active){
                    jTabbedPane2.setSelectedIndex(5);
                }else{
                    registerMsg();
                }
            }
        });

        jTabbedPane4.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

        jPanel43.setBackground(new java.awt.Color(255, 255, 255));

        jPanel44.setBackground(new java.awt.Color(255, 255, 255));

        jLabel209.setFont(new java.awt.Font("Microsoft Yi Baiti", 1, 18)); // NOI18N
        jLabel209.setForeground(new java.awt.Color(55, 150, 198));
        jLabel209.setText("StartUp Page:");

        jComboBox6.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Selection", "Home", "Sales", "Update Stock", "Search & Glance", "Others", "Settings" }));
        jComboBox6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox6ItemStateChanged(evt);
            }
        });

        jLabel210.setFont(new java.awt.Font("Microsoft Yi Baiti", 1, 18)); // NOI18N
        jLabel210.setForeground(new java.awt.Color(55, 150, 198));
        jLabel210.setText("Password Change:");

        jButton23.setBackground(new java.awt.Color(255, 255, 255));
        jButton23.setForeground(new java.awt.Color(55, 150, 198));
        jButton23.setText("Change");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jLabel211.setFont(new java.awt.Font("Microsoft Yi Baiti", 1, 18)); // NOI18N
        jLabel211.setForeground(new java.awt.Color(55, 150, 198));
        jLabel211.setText("Clear Selling Details:");

        jLabel212.setBackground(new java.awt.Color(55, 150, 198));
        jLabel212.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel212.setForeground(new java.awt.Color(255, 255, 255));
        jLabel212.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel212.setText("Today's");
        jLabel212.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel212.setOpaque(true);
        jLabel212.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel212MouseClicked(evt);
            }
        });

        jLabel213.setBackground(new java.awt.Color(55, 150, 198));
        jLabel213.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel213.setForeground(new java.awt.Color(255, 255, 255));
        jLabel213.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel213.setText("Last Day");
        jLabel213.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel213.setOpaque(true);
        jLabel213.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel213MouseClicked(evt);
            }
        });

        jLabel214.setBackground(new java.awt.Color(55, 150, 198));
        jLabel214.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel214.setForeground(new java.awt.Color(255, 255, 255));
        jLabel214.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel214.setText("Clear Details");
        jLabel214.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel214.setOpaque(true);
        jLabel214.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel214MouseClicked(evt);
            }
        });

        jLabel215.setBackground(new java.awt.Color(55, 150, 198));
        jLabel215.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel215.setForeground(new java.awt.Color(255, 255, 255));
        jLabel215.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel215.setText("Clear Details");
        jLabel215.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel215.setOpaque(true);
        jLabel215.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel215MouseClicked(evt);
            }
        });

        jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));

        jLabel216.setBackground(new java.awt.Color(55, 150, 198));
        jLabel216.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel216.setForeground(new java.awt.Color(255, 255, 255));
        jLabel216.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel216.setText("Clear Details");
        jLabel216.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel216.setOpaque(true);
        jLabel216.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel216MouseClicked(evt);
            }
        });

        jComboBox9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));

        jPasswordField3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPasswordField3.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jPasswordField4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPasswordField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jPasswordField5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jPasswordField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel245.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel245.setForeground(new java.awt.Color(55, 150, 198));
        jLabel245.setText("Current:");

        jLabel246.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel246.setForeground(new java.awt.Color(55, 150, 198));
        jLabel246.setText("New:");

        jLabel247.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel247.setForeground(new java.awt.Color(55, 150, 198));
        jLabel247.setText("Re-Type:");

        jLabel248.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel248.setForeground(new java.awt.Color(55, 150, 198));
        jLabel248.setText("Choose a day:");

        jLabel249.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel249.setForeground(new java.awt.Color(55, 150, 198));
        jLabel249.setText("Choose a month:");

        jLabel250.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel250.setForeground(new java.awt.Color(55, 150, 198));
        jLabel250.setText("Choose a year:");

        jLabel70.setForeground(new java.awt.Color(0, 204, 0));
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel250, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel44Layout.createSequentialGroup()
                                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel44Layout.createSequentialGroup()
                                        .addComponent(jXDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel44Layout.createSequentialGroup()
                                        .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(10, 10, 10)))
                                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel214, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel216, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel44Layout.createSequentialGroup()
                                .addComponent(jLabel209, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel211, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel44Layout.createSequentialGroup()
                                .addComponent(jLabel210, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel44Layout.createSequentialGroup()
                                        .addGap(209, 209, 209)
                                        .addComponent(jButton23))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel44Layout.createSequentialGroup()
                                        .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel245, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel247, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel246, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jPasswordField5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                                            .addComponent(jPasswordField4, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jPasswordField3, javax.swing.GroupLayout.Alignment.TRAILING)))))
                            .addGroup(jPanel44Layout.createSequentialGroup()
                                .addComponent(jLabel212, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel213, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel44Layout.createSequentialGroup()
                                .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel215, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel248, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel249, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 9, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel44Layout.createSequentialGroup()
                        .addComponent(jLabel70, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel44Layout.setVerticalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addComponent(jLabel70, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel209))
                .addGap(19, 19, 19)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel210)
                    .addComponent(jPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel245))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jPasswordField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel246))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jPasswordField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel247))
                .addGap(4, 4, 4)
                .addComponent(jButton23)
                .addGap(9, 9, 9)
                .addComponent(jLabel211)
                .addGap(18, 18, 18)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel212, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel213, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel248)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jXDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel214, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(jLabel249)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel215, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel250)
                .addGap(7, 7, 7)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel216, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jPasswordField3.setEchoChar('●');
        jPasswordField4.setEchoChar('●');
        jPasswordField5.setEchoChar('●');

        jPanel45.setBackground(new java.awt.Color(255, 255, 255));

        jLabel218.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel218.setForeground(new java.awt.Color(55, 150, 198));
        jLabel218.setText("Licensee Details:");

        jLabel221.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel221.setForeground(new java.awt.Color(55, 150, 198));
        jLabel221.setText("Email:");

        jLabel220.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel220.setText("Not Registered");

        jLabel222.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel222.setForeground(new java.awt.Color(55, 150, 198));
        jLabel222.setText("Name:");

        jLabel223.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 20)); // NOI18N
        jLabel223.setText("Not Registered");

        jLabel224.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel224.setForeground(new java.awt.Color(55, 150, 198));
        jLabel224.setText("UserId:");

        jLabel225.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 20)); // NOI18N
        jLabel225.setText("Not Registered");

        jLabel226.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel226.setForeground(new java.awt.Color(55, 150, 198));
        jLabel226.setText("Product Id:");

        jLabel227.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 20)); // NOI18N
        jLabel227.setText("Not Registered");

        jLabel228.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jLabel228.setForeground(new java.awt.Color(55, 150, 198));
        jLabel228.setText("Product Key:");

        jLabel229.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 20)); // NOI18N
        jLabel229.setText("Not Registered");

        jLabel217.setBackground(new java.awt.Color(55, 150, 198));
        jLabel217.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel217.setForeground(new java.awt.Color(255, 255, 255));
        jLabel217.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel217.setText("Forgotten your password?");
        jLabel217.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel217.setOpaque(true);
        jLabel217.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel217MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel221)
                            .addComponent(jLabel224)
                            .addComponent(jLabel222))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel220, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel223, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel225, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addComponent(jLabel218, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel45Layout.createSequentialGroup()
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel217, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel45Layout.createSequentialGroup()
                                .addComponent(jLabel226)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel227, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel45Layout.createSequentialGroup()
                                .addComponent(jLabel228, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel229, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(28, 28, 28))))
        );
        jPanel45Layout.setVerticalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel218)
                .addGap(18, 18, 18)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel220)
                    .addComponent(jLabel222))
                .addGap(18, 18, 18)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel221)
                    .addComponent(jLabel223))
                .addGap(18, 18, 18)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel224)
                    .addComponent(jLabel225))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel226)
                    .addComponent(jLabel227))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel228)
                    .addComponent(jLabel229))
                .addGap(41, 41, 41)
                .addComponent(jLabel217, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout.setHorizontalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel43Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jPanel45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel43Layout.setVerticalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel43Layout.createSequentialGroup()
                .addContainerGap(42, Short.MAX_VALUE)
                .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel44, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jTabbedPane4.addTab("tab1", jPanel43);

        jPanel46.setBackground(new java.awt.Color(255, 255, 255));

        jLabel231.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel231.setForeground(new java.awt.Color(55, 150, 198));
        jLabel231.setText("Register Your Product");

        jLabel232.setBackground(new java.awt.Color(55, 150, 198));
        jLabel232.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel232.setForeground(new java.awt.Color(255, 255, 255));
        jLabel232.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel232.setText("I don't have a product Key");
        jLabel232.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel232.setOpaque(true);
        jLabel232.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel232MouseClicked(evt);
            }
        });

        jLabel233.setBackground(new java.awt.Color(55, 150, 198));
        jLabel233.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel233.setForeground(new java.awt.Color(255, 255, 255));
        jLabel233.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel233.setText("I have a product key");
        jLabel233.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel233.setOpaque(true);
        jLabel233.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel233MouseClicked(evt);
            }
        });

        jTabbedPane5.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane5.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

        jPanel47.setBackground(new java.awt.Color(255, 255, 255));

        jTabbedPane6.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

        jPanel36.setBackground(new java.awt.Color(255, 255, 255));

        jLabel67.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addComponent(jLabel67, javax.swing.GroupLayout.PREFERRED_SIZE, 940, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 40, Short.MAX_VALUE))
        );
        jPanel36Layout.setVerticalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel67, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
        );

        jTabbedPane6.addTab("", jPanel36);

        jPanel37.setBackground(new java.awt.Color(255, 255, 255));

        jPanel38.setBackground(new java.awt.Color(255, 255, 255));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/medistore/ring (3).gif"))); // NOI18N

        jLabel258.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel258.setForeground(new java.awt.Color(55, 150, 198));
        jLabel258.setText("Loading...");

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel38Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel258)
                .addGap(82, 82, 82))
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel38Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel258))
        );

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37.setLayout(jPanel37Layout);
        jPanel37Layout.setHorizontalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addGap(322, 322, 322)
                .addComponent(jPanel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(366, Short.MAX_VALUE))
        );
        jPanel37Layout.setVerticalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel37Layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(jPanel38, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );

        jTabbedPane6.addTab("", jPanel37);

        javax.swing.GroupLayout jPanel52Layout = new javax.swing.GroupLayout(jPanel52);
        jPanel52.setLayout(jPanel52Layout);
        jPanel52Layout.setHorizontalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 980, Short.MAX_VALUE)
        );
        jPanel52Layout.setVerticalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 363, Short.MAX_VALUE)
        );

        jTabbedPane6.addTab("", jPanel52);

        javax.swing.GroupLayout jPanel47Layout = new javax.swing.GroupLayout(jPanel47);
        jPanel47.setLayout(jPanel47Layout);
        jPanel47Layout.setHorizontalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane6)
        );
        jPanel47Layout.setVerticalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel47Layout.createSequentialGroup()
                .addComponent(jTabbedPane6)
                .addContainerGap())
        );

        jTabbedPane5.addTab("", jPanel47);

        jPanel48.setBackground(new java.awt.Color(255, 255, 255));

        jLabel234.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 30)); // NOI18N
        jLabel234.setForeground(new java.awt.Color(55, 150, 198));
        jLabel234.setText("Your Product Key:");

        jTextField28.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField28.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField28.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField28FocusGained(evt);
            }
        });
        jTextField28.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField28KeyReleased(evt);
            }
        });

        jTextField29.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField29.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField29.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField29FocusGained(evt);
            }
        });
        jTextField29.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField29KeyReleased(evt);
            }
        });

        jTextField30.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField30.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField30.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField30FocusGained(evt);
            }
        });
        jTextField30.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField30KeyReleased(evt);
            }
        });

        jTextField31.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField31.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField31.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField31FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField31FocusLost(evt);
            }
        });
        jTextField31.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField31KeyReleased(evt);
            }
        });

        jLabel236.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 30)); // NOI18N
        jLabel236.setForeground(new java.awt.Color(55, 150, 198));
        jLabel236.setText("Your Name:");

        jTextField32.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField32.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField32ActionPerformed(evt);
            }
        });

        jTextField34.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField34.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField34.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField34FocusLost(evt);
            }
        });

        jLabel238.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 30)); // NOI18N
        jLabel238.setForeground(new java.awt.Color(55, 150, 198));
        jLabel238.setText("Email:");

        jLabel239.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 30)); // NOI18N
        jLabel239.setForeground(new java.awt.Color(55, 150, 198));
        jLabel239.setText("Contact No.");

        jTextField35.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jTextField35.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel240.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 30)); // NOI18N
        jLabel240.setForeground(new java.awt.Color(55, 150, 198));
        jLabel240.setText("Choose a password:");

        jLabel241.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 30)); // NOI18N
        jLabel241.setForeground(new java.awt.Color(55, 150, 198));
        jLabel241.setText("Re-password:");

        jCheckBox2.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBox2.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 18)); // NOI18N
        jCheckBox2.setForeground(new java.awt.Color(55, 150, 198));
        jCheckBox2.setText("I have read and agree to the Terms & Conditions.");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jLabel242.setBackground(new java.awt.Color(55, 150, 198));
        jLabel242.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        jLabel242.setForeground(new java.awt.Color(255, 255, 255));
        jLabel242.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel242.setText("Terms & Conditions");
        jLabel242.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel242.setOpaque(true);
        jLabel242.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel242MouseClicked(evt);
            }
        });

        jLabel243.setBackground(new java.awt.Color(55, 150, 198));
        jLabel243.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel243.setForeground(new java.awt.Color(255, 255, 255));
        jLabel243.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel243.setText("Register");
        jLabel243.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel243.setOpaque(true);
        jLabel243.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel243MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel243MouseEntered(evt);
            }
        });

        jPasswordField1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jPasswordField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jPasswordField2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jPasswordField2.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/medistore/ring (2).gif"))); // NOI18N
        jLabel4.setOpaque(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel48Layout = new javax.swing.GroupLayout(jPanel48);
        jPanel48.setLayout(jPanel48Layout);
        jPanel48Layout.setHorizontalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel48Layout.createSequentialGroup()
                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel236)
                            .addComponent(jLabel238)
                            .addComponent(jLabel239))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField34)
                            .addComponent(jTextField32, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel48Layout.createSequentialGroup()
                                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel240)
                                    .addComponent(jLabel241))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel48Layout.createSequentialGroup()
                                .addComponent(jCheckBox2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel242, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(64, 64, 64))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel243, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(324, 324, 324))
                    .addGroup(jPanel48Layout.createSequentialGroup()
                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel48Layout.createSequentialGroup()
                                .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19)
                                .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel235, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel234))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel48Layout.setVerticalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(jLabel234)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel235, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                                .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel236)
                                        .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel48Layout.createSequentialGroup()
                                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel240)
                                            .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel241)
                                            .addComponent(jLabel238)
                                            .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(10, 10, 10)
                                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jCheckBox2)
                                    .addComponent(jLabel239)
                                    .addComponent(jLabel242, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(104, 104, 104))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                        .addComponent(jLabel243, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(48, 48, 48))))
        );

        jLabel164.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });
        jPasswordField1.setEchoChar('●');
        jPasswordField2.setEchoChar('●');
        jPanel1.setVisible(false);

        jTabbedPane5.addTab("", jPanel48);

        jPanel49.setBackground(new java.awt.Color(255, 255, 255));

        jPanel20.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/medistore/ring (3).gif"))); // NOI18N

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel49Layout = new javax.swing.GroupLayout(jPanel49);
        jPanel49.setLayout(jPanel49Layout);
        jPanel49Layout.setHorizontalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addGap(318, 318, 318)
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(383, Short.MAX_VALUE))
        );
        jPanel49Layout.setVerticalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jTabbedPane5.addTab("", jPanel49);

        javax.swing.GroupLayout jPanel46Layout = new javax.swing.GroupLayout(jPanel46);
        jPanel46.setLayout(jPanel46Layout);
        jPanel46Layout.setHorizontalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel233, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel232, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(78, 78, 78))
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addGap(352, 352, 352)
                .addComponent(jLabel231)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane5))
        );
        jPanel46Layout.setVerticalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel231)
                .addGap(18, 18, 18)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel233, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel232, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane5)
                .addContainerGap())
        );

        jTabbedPane4.addTab("tab2", jPanel46);

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );
        jPanel42Layout.setVerticalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel42Layout.createSequentialGroup()
                .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 525, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("settings", jPanel42);

        jPanel31.setBackground(new java.awt.Color(255, 255, 255));

        jPanel51.setBackground(new java.awt.Color(0, 0, 0));

        jLabel219.setBackground(new java.awt.Color(140, 56, 85));
        jLabel219.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel219.setForeground(new java.awt.Color(255, 255, 255));
        jLabel219.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel219.setText("Home");
        jLabel219.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel219.setOpaque(true);

        jLabel230.setBackground(new java.awt.Color(140, 56, 85));
        jLabel230.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel230.setForeground(new java.awt.Color(255, 255, 255));
        jLabel230.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel230.setText("Sales");
        jLabel230.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel230.setOpaque(true);

        jLabel251.setBackground(new java.awt.Color(140, 56, 85));
        jLabel251.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel251.setForeground(new java.awt.Color(255, 255, 255));
        jLabel251.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel251.setText("Updated Stock");
        jLabel251.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel251.setOpaque(true);

        jLabel252.setBackground(new java.awt.Color(255, 255, 255));
        jLabel252.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel252.setForeground(new java.awt.Color(140, 56, 85));
        jLabel252.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel252.setText("Search & Glance");
        jLabel252.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel252.setOpaque(true);

        jLabel253.setBackground(new java.awt.Color(140, 56, 85));
        jLabel253.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel253.setForeground(new java.awt.Color(255, 255, 255));
        jLabel253.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel253.setText("Others");
        jLabel253.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel253.setOpaque(true);

        jLabel254.setBackground(new java.awt.Color(140, 56, 85));
        jLabel254.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel254.setForeground(new java.awt.Color(255, 255, 255));
        jLabel254.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel254.setText("Settings");
        jLabel254.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel254.setOpaque(true);

        javax.swing.GroupLayout jPanel51Layout = new javax.swing.GroupLayout(jPanel51);
        jPanel51.setLayout(jPanel51Layout);
        jPanel51Layout.setHorizontalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addComponent(jLabel219, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel230, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel251, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel252, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel253, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel254, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(109, Short.MAX_VALUE))
        );
        jPanel51Layout.setVerticalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel219, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel251, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel252, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel230, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel253, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel254, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel219.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(0);
                refresh_jTable2();
                refresh_jTable3();

            }
        });
        jLabel230.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(2);
            }
        });
        jLabel251.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(1);
            }
        });
        jLabel253.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(3);
            }
        });
        jLabel254.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                jTabbedPane2.setSelectedIndex(4);
            }
        });

        jPanel40.setOpaque(false);

        jLabel183.setBackground(new java.awt.Color(140, 56, 85));
        jLabel183.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel183.setForeground(new java.awt.Color(255, 255, 255));
        jLabel183.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel183.setText("In-Stock");
        jLabel183.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel183.setOpaque(true);
        jLabel183.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel183MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel183MouseEntered(evt);
            }
        });

        jLabel184.setBackground(new java.awt.Color(140, 56, 85));
        jLabel184.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel184.setForeground(new java.awt.Color(255, 255, 255));
        jLabel184.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel184.setText("Out-of-Stock");
        jLabel184.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel184.setOpaque(true);
        jLabel184.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel184MouseClicked(evt);
            }
        });

        jLabel185.setBackground(new java.awt.Color(140, 56, 85));
        jLabel185.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel185.setForeground(new java.awt.Color(255, 255, 255));
        jLabel185.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel185.setText("Search");
        jLabel185.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel185.setOpaque(true);
        jLabel185.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel185MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addComponent(jLabel185, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel183, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel184, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel185, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel183, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel184, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
        );

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setForeground(new java.awt.Color(140, 56, 85));
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(140, 56, 85), 2, true));
        jPanel11.setFocusable(false);
        jPanel11.setRequestFocusEnabled(false);
        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(140, 56, 85));
        jLabel12.setText("Composition matching Medicines:");
        jPanel11.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, -1, -1));

        jButton5.setBackground(new java.awt.Color(255, 255, 255));
        jButton5.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jButton5.setForeground(new java.awt.Color(140, 56, 85));
        jButton5.setText("Search");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel11.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 130, -1, -1));

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });
        jPanel11.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 100, 170, -1));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel25.setText("Search by medicine composition:");
        jPanel11.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        jTable5.setForeground(new java.awt.Color(140, 56, 85));
        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Name", "Composition1", "Composition2", "Composition3"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable5.setOpaque(true);
        jTable5.setFillsViewportHeight(true);
        jTable5.setBackground(new Color(255, 255, 255));
        jTable5.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane7.setViewportView(jTable5);

        jPanel11.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 183, 266, 200));

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });
        jPanel11.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 70, 170, -1));

        jTextField7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField7ActionPerformed(evt);
            }
        });
        jPanel11.add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 40, 170, -1));

        jLabel75.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel75.setForeground(new java.awt.Color(140, 56, 85));
        jLabel75.setText("Composition3:");
        jPanel11.add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        jLabel111.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel111.setForeground(new java.awt.Color(140, 56, 85));
        jLabel111.setText("Composition2:");
        jPanel11.add(jLabel111, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        jLabel112.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel112.setForeground(new java.awt.Color(140, 56, 85));
        jLabel112.setText("Composition1:");
        jPanel11.add(jLabel112, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));
        jPanel13.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(140, 56, 85), 2, true));
        jPanel13.setFocusable(false);
        jPanel13.setRequestFocusEnabled(false);
        jPanel13.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel73.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel73.setForeground(new java.awt.Color(140, 56, 85));
        jPanel13.add(jLabel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 150, -1));

        jButton12.setBackground(new java.awt.Color(255, 255, 255));
        jButton12.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jButton12.setForeground(new java.awt.Color(140, 56, 85));
        jButton12.setText("SHORTLIST THIS");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        jPanel13.add(jButton12, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 70, -1, -1));

        jLabel74.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jPanel13.add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, 80, -1));

        jLabel76.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel76.setForeground(new java.awt.Color(140, 56, 85));
        jLabel76.setText("Stock Location:");
        jPanel13.add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        jLabel77.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel77.setForeground(new java.awt.Color(140, 56, 85));
        jLabel77.setText("MRP:");
        jPanel13.add(jLabel77, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 110, -1, -1));

        jLabel79.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel79.setForeground(new java.awt.Color(140, 56, 85));
        jLabel79.setText("Compositions: ");
        jPanel13.add(jLabel79, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, -1, -1));

        jLabel80.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jPanel13.add(jLabel80, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 172, -1, -1));

        jLabel102.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel102.setForeground(new java.awt.Color(140, 56, 85));
        jLabel102.setText("Descriptions:");
        jPanel13.add(jLabel102, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, -1));

        jLabel103.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel103.setForeground(new java.awt.Color(140, 56, 85));
        jLabel103.setText("Note:");
        jPanel13.add(jLabel103, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 200, -1, -1));

        jLabel104.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel104.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel13.add(jLabel104, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 70, -1));

        jLabel105.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel105.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel105.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel13.add(jLabel105, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 100, -1, -1));

        jLabel106.setForeground(new java.awt.Color(140, 140, 140));
        jLabel106.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel13.add(jLabel106, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 130, 50));

        jLabel107.setForeground(new java.awt.Color(140, 140, 140));
        jLabel107.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel13.add(jLabel107, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 220, 130, 50));

        jButton13.setBackground(new java.awt.Color(255, 255, 255));
        jButton13.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jButton13.setForeground(new java.awt.Color(140, 56, 85));
        jButton13.setText("Search");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jPanel13.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(219, 41, -1, -1));

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });
        jPanel13.add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 42, 191, -1));

        jLabel108.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel108.setText("Search by medicine name:");
        jPanel13.add(jLabel108, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        jLabel109.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel109.setForeground(new java.awt.Color(140, 56, 85));
        jLabel109.setText("/unit");
        jPanel13.add(jLabel109, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 110, -1, -1));

        jLabel110.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel110.setForeground(new java.awt.Color(140, 56, 85));
        jLabel110.setText("In-Stock :");
        jPanel13.add(jLabel110, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        jTable11.setForeground(new java.awt.Color(140, 56, 85));
        jTable11.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Other Matches", "Availability"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable11.setOpaque(true);
        jTable11.setFillsViewportHeight(true);
        jTable11.setBackground(new Color(255, 255, 255));
        jTable11.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane13.setViewportView(jTable11);
        if (jTable11.getColumnModel().getColumnCount() > 0) {
            jTable11.getColumnModel().getColumn(0).setResizable(false);
            jTable11.getColumnModel().getColumn(1).setResizable(false);
        }

        jPanel13.add(jScrollPane13, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 270, 110));

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(140, 56, 85), 2, true));
        jPanel14.setFocusable(false);
        jPanel14.setRequestFocusEnabled(false);
        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton15.setBackground(new java.awt.Color(255, 255, 255));
        jButton15.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        jButton15.setForeground(new java.awt.Color(140, 56, 85));
        jButton15.setText("Search");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton15, new org.netbeans.lib.awtextra.AbsoluteConstraints(205, 41, -1, -1));

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });
        jPanel14.add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(19, 42, 180, -1));

        jLabel126.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel126.setText("Free Search:");
        jPanel14.add(jLabel126, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        jTable6.setForeground(new java.awt.Color(140, 56, 85));
        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Compositions:"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable6.setOpaque(true);
        jTable6.setFillsViewportHeight(true);
        jTable6.setBackground(new Color(255, 255, 255));
        jTable6.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane8.setViewportView(jTable6);
        if (jTable6.getColumnModel().getColumnCount() > 0) {
            jTable6.getColumnModel().getColumn(0).setResizable(false);
        }

        jPanel14.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, 126, 283));

        jTable7.setForeground(new java.awt.Color(140, 56, 185));
        jTable7.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Medicines:"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable7.setOpaque(true);
        jTable7.setFillsViewportHeight(true);
        jTable7.setBackground(new Color(255, 255, 255));
        jTable7.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane9.setViewportView(jTable7);
        if (jTable7.getColumnModel().getColumnCount() > 0) {
            jTable7.getColumnModel().getColumn(0).setResizable(false);
        }

        jPanel14.add(jScrollPane9, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 100, 126, 283));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(140, 56, 85));
        jLabel26.setText("Search matching Results:");
        jPanel14.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        jLabel257.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel257.setForeground(new java.awt.Color(140, 56, 85));
        jLabel257.setText("Search");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel257, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel257)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28))
        );

        jPanel4.setVisible(false);
        jPanel4.setVisible(false);
        jPanel4.setVisible(false);

        jTabbedPane1.addTab("1", jPanel6);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(140, 56, 85), 2, true));
        jPanel4.setFocusable(false);
        jPanel4.setRequestFocusEnabled(false);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(140, 56, 85));
        jLabel8.setText("Available in Stock");

        jLabel7.setBackground(new java.awt.Color(140, 56, 85));
        jLabel7.setFont(new java.awt.Font("Felix Titling", 0, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("MED_NAME");
        jLabel7.setOpaque(true);

        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setForeground(new java.awt.Color(140, 56, 85));
        jButton2.setText("CLOSE");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 255, 255));
        jButton3.setForeground(new java.awt.Color(140, 56, 85));
        jButton3.setText("SHORTLIST THIS");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel10.setText("1525");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(140, 56, 85));
        jLabel9.setText("In-Stock :");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(140, 56, 85));
        jLabel11.setText("Stock Location:");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(140, 56, 85));
        jLabel13.setText("MRP:");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(140, 56, 85));
        jLabel15.setText("Other Details");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(140, 56, 85));
        jLabel16.setText("Compositions: ");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel17.setText("jLabel10");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(140, 56, 85));
        jLabel18.setText("Descriptions:");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(140, 56, 85));
        jLabel19.setText("Note:");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("1525");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel22.setText("1525");

        jLabel23.setText("jLabel10");
        jLabel23.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel24.setText("jLabel10");
        jLabel24.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(140, 56, 85));
        jLabel14.setText("/unit");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel17))
                            .addComponent(jLabel18)
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton3)
                                .addGap(2, 2, 2)
                                .addComponent(jButton2))))
                    .addComponent(jLabel8)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14))
                    .addComponent(jLabel15))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addGap(1, 1, 1)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13)
                    .addComponent(jLabel21)
                    .addComponent(jLabel22)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(28, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(jButton2))
                        .addContainerGap())))
        );

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 40, 580, -1));
        jPanel4.setVisible(false);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel3.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(735, 12, 173, -1));

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setForeground(new java.awt.Color(140, 56, 85));
        jButton1.setText("Search");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(915, 10, -1, -1));

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Available Quantity", "MRP", "Stock Location", "Details"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable3.setGridColor(new java.awt.Color(255, 255, 255));
        jTable3.setInheritsPopupMenu(true);
        jTable3.setOpaque(false);
        jTable3.getTableHeader().setReorderingAllowed(false);
        jTable3.setOpaque(true);
        jTable3.setFillsViewportHeight(true);
        jTable3.setBackground(new Color(255, 255, 255));
        jTable3.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane5.setViewportView(jTable3);
        if (jTable3.getColumnModel().getColumnCount() > 0) {
            jTable3.getColumnModel().getColumn(0).setResizable(false);
            jTable3.getColumnModel().getColumn(1).setResizable(false);
            jTable3.getColumnModel().getColumn(2).setResizable(false);
            jTable3.getColumnModel().getColumn(3).setResizable(false);
            jTable3.getColumnModel().getColumn(4).setResizable(false);
        }
        refresh_jTable3();

        jPanel3.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 970, 360));

        jLabel256.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel256.setForeground(new java.awt.Color(140, 56, 85));
        jLabel256.setText("In-Stock List");
        jPanel3.add(jLabel256, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, -1, -1));

        jTabbedPane1.addTab("2", jPanel3);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(255, 255, 255));
        jButton4.setForeground(new java.awt.Color(140, 56, 85));
        jButton4.setText("Search");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Composition1", "Composition2", "Composition3", "Note"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.setGridColor(new java.awt.Color(255, 255, 255));
        jTable2.setInheritsPopupMenu(true);
        jTable2.getTableHeader().setReorderingAllowed(false);
        jTable2.setOpaque(true);
        jTable2.setFillsViewportHeight(true);
        jTable2.setBackground(new Color(255, 255, 255));
        jTable2.getTableHeader().setBackground(new Color(255, 255, 255));
        jScrollPane2.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(0).setResizable(false);
            jTable2.getColumnModel().getColumn(1).setResizable(false);
            jTable2.getColumnModel().getColumn(2).setResizable(false);
            jTable2.getColumnModel().getColumn(3).setResizable(false);
            jTable2.getColumnModel().getColumn(4).setResizable(false);
        }
        refresh_jTable2();

        jLabel255.setFont(new java.awt.Font("Microsoft Yi Baiti", 0, 36)); // NOI18N
        jLabel255.setForeground(new java.awt.Color(140, 56, 85));
        jLabel255.setText("Out-of-Stock List");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel255)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 503, Short.MAX_VALUE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addContainerGap(63, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4)))
                    .addComponent(jLabel255))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(71, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("3", jPanel5);

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jPanel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel40, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("search glance", jPanel31);

        getContentPane().add(jTabbedPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1090, 640));
        /*jTabbedPane2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                try {
                    Desktop desktop=Desktop.getDesktop();
                    try{
                        desktop.browse(new URI("http://sybero.com"));
                    }
                    catch(IOException e){
                        System.out.println("err::"+e.getMessage());
                    }
                }
                catch (URISyntaxException e) {
                    System.out.println("error found::"+e.getMessage());
                }
            }
        });*/

        jTextArea4.setColumns(20);
        jTextArea4.setRows(5);
        jScrollPane4.setViewportView(jTextArea4);

        getContentPane().add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 690, 70, 60));

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 690, 70, 53));

        jButton7.setText("jButton7");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 690, -1, -1));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Composition1", "Composition2", "Composition3", "Note"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable4.setGridColor(new java.awt.Color(255, 255, 255));
        jTable4.setInheritsPopupMenu(true);
        jTable4.getTableHeader().setReorderingAllowed(false);
        jScrollPane6.setViewportView(jTable4);
        if (jTable4.getColumnModel().getColumnCount() > 0) {
            jTable4.getColumnModel().getColumn(0).setResizable(false);
            jTable4.getColumnModel().getColumn(1).setResizable(false);
            jTable4.getColumnModel().getColumn(2).setResizable(false);
            jTable4.getColumnModel().getColumn(3).setResizable(false);
            jTable4.getColumnModel().getColumn(4).setResizable(false);
        }

        getContentPane().add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 690, 90, 60));

        getContentPane().add(jComboBox5, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 690, -1, -1));

        jLabel68.setText("jLabel4");
        getContentPane().add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 690, -1, -1));

        javax.swing.GroupLayout jPanel58Layout = new javax.swing.GroupLayout(jPanel58);
        jPanel58.setLayout(jPanel58Layout);
        jPanel58Layout.setHorizontalGroup(
            jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel58Layout.setVerticalGroup(
            jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 830, -1, -1));

        jLabel164.setText("jLabel42");
        getContentPane().add(jLabel164, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 690, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    
    void reset_jPanel2(){
                    jTextField20.setEnabled(false);
                    jTextField10.setEnabled(false);
                    jTextField9.setEnabled(false);
                    jTextField20.setText("");
                    jTextField10.setText("");
                    jTextField9.setText("");
                    jTextField11.setText("");
                    jTextField12.setText("");
                    jTextField13.setText("");
                    jTextField14.setText("");
                    jEditorPane1.setText("");
                    jEditorPane2.setText("");
                    JOptionPane.showMessageDialog(this, "Your request has been processed succesfully.");
                    jCheckBox1.setSelected(false);
                }
    
    int show_newAddition_warning=0;
    private void final_setup(String text_name, int t1, int text_quantity, int t){
        DefaultTableModel model = (DefaultTableModel)jTable1.getModel();
        
        model.addRow(new Object[] {text_name,t1,text_quantity,t});
        jTextField15.setText("");
        jTextField16.setText("");
        jTextField19.setText("");
        jTextField22.setText("");
    }
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        // TODO add your handling code here:
        jButton20ActionPerformed(evt);
        if(!jTextField17.getText().equals("") && !jTextField21.getText().equals("") && Double.valueOf(jTextField21.getText())>0){
            
            Connection con; ResultSet rs;
            String search_name=jTextField17.getText();
            Double price=0.0;
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                rs = stmt.executeQuery("select * from STOCK");

                boolean search_found=false;
                int quantity=0;
                while(rs.next()){
                    if(rs.getString("MED_NAME").equals(search_name)){
                        price=rs.getDouble("PRICE");
                        search_found=true;
                        quantity = rs.getInt("QUANTITY")-Integer.valueOf(jTextField21.getText());
                    }
                }
                
                if(search_found){
                    if(!jTextField18.getText().equals("")){
                        if(Double.valueOf(jTextField18.getText())>=0){
                            price=Double.valueOf(jTextField18.getText());
                        }
                        else{
                            JOptionPane.showMessageDialog(this, "You have entered an invalid input. The amount has been calculated according to the saved details.");
                        }
                    }
                    
                    Double amount=price*Double.valueOf(jTextField21.getText());
                    jLabel87.setText(String.valueOf(amount));

                    
                    if(quantity>0){
                        stmt.executeUpdate("UPDATE STOCK SET QUANTITY="+quantity+" WHERE CAST(MED_NAME AS VARCHAR(128))='"+search_name+"'");
                    }
                    else if(quantity==0){
                        stmt.executeUpdate("DELETE FROM STOCK WHERE CAST(MED_NAME AS VARCHAR(128))='"+search_name+"'");
                    }
                    else{
                        JOptionPane.showMessageDialog(this, "The medicine is not available in your stock");
                    }
                    
                    jTextField21.setText("");
                    jTextField18.setText("");
                    jTextField17.setText("");
                    jLabel82.setText("0");
                    jLabel86.setText("0.0");
                    jLabel173.setText("NA");
                    jEditorPane3.setText("");
                    jEditorPane4.setText("");
                    jEditorPane5.setText("");
                    jLabel87.setText("0.0");
                }
                else {
                    jLabel1.setText("Not available in stock!!!");
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
        else{
            
            if(jTextField17.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please enter medicine name!!!");
            }
            else if(jTextField21.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Quantity field shouldn't be vacant!!!");
            }
            else if(Double.valueOf(jTextField21.getText())<=0) {
                JOptionPane.showMessageDialog(this, "Quantity field should be filled with valid input!!!");
            }
            else{
                JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            }
        }
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model =(DefaultTableModel)jTable12.getModel();
        if(model.getRowCount()!=0){
            for(int i=0;i<model.getRowCount();i++){
                String search_name=(String)model.getValueAt(i, 0);
                int quantity=(Integer)model.getValueAt(i, 1);

                Connection con; ResultSet rs;

                try {
                    String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                    con=(Connection)DriverManager.getConnection(connectionURL);

                    Statement stmt=con.createStatement();
                    rs = stmt.executeQuery("select * from STOCK");

                    while(rs.next()){
                        if(rs.getString("MED_NAME").equals(search_name)){
                            quantity = rs.getInt("QUANTITY")-quantity;
                        }
                    }

                    if(quantity>0){
                        stmt.executeUpdate("UPDATE STOCK SET QUANTITY="+quantity+" WHERE CAST(MED_NAME AS VARCHAR(128))='"+search_name+"'");
                    }
                    else if(quantity==0){
                        stmt.executeUpdate("DELETE FROM STOCK WHERE CAST(MED_NAME AS VARCHAR(128))='"+search_name+"'");
                    }
                    model.setNumRows(0);
                    jTextField17.setText("");
                    jTextField18.setText("");
                    jTextField21.setText("");
                    jLabel82.setText("0");
                    jLabel86.setText("0.0");
                    jLabel87.setText("");
                    jLabel173.setText("NA");
                    jEditorPane3.setText("");
                    jEditorPane4.setText("");
                    jEditorPane5.setText("");
                }
                catch(SQLException e){
                    System.out.println(e.getMessage());
                    e.getStackTrace();
                }
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Please some medicines first!!!");
        }
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jTextField17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField17ActionPerformed
        // TODO add your handling code here:
        jButton20ActionPerformed(evt);
    }//GEN-LAST:event_jTextField17ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        String search_name=jTextField2.getText();
        if (!search_name.equals("")){
            Connection con; ResultSet rs;
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                String query="select * from STOCK";

                rs = stmt.executeQuery(query);

                boolean present=false;
                while(rs.next()){
                    if(search_name.equals(rs.getString("MED_NAME"))){
                        //JOptionPane.showMessageDialog(this, "'"+search_name+"' is no more in your stock");
                        present=true;
                    }
                }

                if (!present){

                    Object[] options = {"YES, SHORTLIST THIS", "NO, NOT YET"};
                    String message =    "'"+search_name+"' is no more in your stock."
                    + "\nWould you like to shortlist this medicine?";

                    if(JOptionPane.showOptionDialog(null, message,"Warning",
                        JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0])==JOptionPane.YES_OPTION){
                        shortlist(search_name);
                    refresh_jTable16();
                }
            }
            else{
                JOptionPane.showMessageDialog(this, "'"+search_name+"' is in your stock.");
            }

            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
        jButton4.requestFocus(true);
        jButton4ActionPerformed(evt);
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if ((jTextField1.getText()).equals("")){
            JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            jTextField1.requestFocus(true);
        }
        else{
            Connection con; ResultSet rs;
            String Search_name=jTextField1.getText();
            int search_found=0;
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                String query="select * from MED_DETAILS";
                rs = stmt.executeQuery(query);

                String Compo1="";
                String Compo2="";
                String Compo3="";
                String Des="";
                String Note="";
                int Quantity=0;
                int Price=0;
                String Location="";

                while(rs.next()){
                    if(Search_name.equals(rs.getString("NAME"))){
                        Compo1=rs.getString("CONFIG1");
                        Compo2=rs.getString("CONFIG2");
                        Compo3=rs.getString("CONFIG3");
                        Des=rs.getString("DESCRIPTION");
                        Note=rs.getString("NOTE");

                        search_found=1;
                    }
                }

                if (search_found==1){
                    query="select * from STOCK";
                    rs = stmt.executeQuery(query);

                    while(rs.next()){
                        if(Search_name.equals(rs.getString("MED_NAME"))){
                            Quantity=rs.getInt("QUANTITY");
                            Price = rs.getInt("PRICE");
                            Location=rs.getString("LOCATION");

                            search_found=2;
                        }
                    }

                    jPanel4.setVisible(true);
                    jScrollPane5.setVisible(false);
                    jTextField1.setEnabled(false);
                    jButton1.setEnabled(false);

                    if (search_found==2){
                        jLabel8.setForeground(Color.green);
                        jLabel8.setText("Available in Stock");
                        jButton3.setVisible(false);
                        jLabel10.setText(String.valueOf(Quantity));
                        jLabel21.setText(Location);
                        jLabel22.setText(String.valueOf(Price));
                    }
                    else{
                        jLabel8.setForeground(Color.red);
                        jLabel8.setText("OOPS!!! Not available in Stock");
                        jButton3.setVisible(true);
                        jLabel10.setText("");
                        jLabel21.setText("");
                        jLabel22.setText("");
                    }

                    jLabel17.setText(Compo1+", "+Compo2+" & "+Compo3);
                    jLabel23.setText(Des);
                    jLabel24.setText(Note);
                    jLabel7.setText(Search_name);
                }
                else{
                    JOptionPane.showMessageDialog(this, "Unable to find any medicine named with '"+Search_name+"'.\nPlease make sure that you have spelled it correctly or it is not in your stock.");

                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }

        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        jButton1ActionPerformed(evt);
        jButton1.requestFocus(true);
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        jPanel4.setVisible(false);
        jTextField1.setEnabled(true);
        jButton1.setEnabled(true);
        jScrollPane5.setVisible(true);
        jTextField1.setText("");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        // TODO add your handling code here:
        jButton15ActionPerformed(evt);
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
        if(jTextField6.getText().equals("")){
            JOptionPane.showMessageDialog(this, "Please fillup the input!!!");
        }
        else{
            String search_name=jTextField6.getText().toLowerCase();
            Connection con; ResultSet rs;

            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                String query="select * from MED_DETAILS";
                rs = stmt.executeQuery(query);

                DefaultTableModel model_med = (DefaultTableModel)jTable7.getModel();
                DefaultTableModel model_compo = (DefaultTableModel)jTable6.getModel();

                model_med.setNumRows(0);
                model_compo.setNumRows(0);
                int med_row_count=0;
                int compo_row_count=0;

                while(rs.next()){
                    if(rs.getString("NAME").toLowerCase().matches("(.*)"+search_name+"(.*)")){
                        model_med.addRow(new Object[] {rs.getString("NAME")});
                        med_row_count++;
                    }

                    if(!rs.getString("CONFIG1").equals("null")){
                        if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+search_name+"(.*)")){
                            model_compo.addRow(new Object[] {rs.getString("CONFIG1")});
                            compo_row_count++;
                        }
                    }
                    if(!rs.getString("CONFIG2").equals("null")){
                        if(rs.getString("CONFIG2").toLowerCase().matches("(.*)"+search_name+"(.*)")){
                            model_compo.addRow(new Object[] {rs.getString("CONFIG2")});
                            compo_row_count++;
                        }
                    }
                    if(!rs.getString("CONFIG3").equals("null")){
                        if(rs.getString("CONFIG3").toLowerCase().matches("(.*)"+search_name+"(.*)")){
                            model_compo.addRow(new Object[] {rs.getString("CONFIG3")});
                            compo_row_count++;
                        }
                    }
                }

                if (med_row_count<15){
                    for(int i=15-med_row_count;i>0;i--){
                        if (i==15){
                            model_med.addRow(new Object[] {"Nothing Found"});

                        }
                        else{
                            model_med.addRow(new Object[] {""});
                        }
                    }
                }
                if (compo_row_count<15){
                    for(int i=15-compo_row_count;i>0;i--){
                        if (i==15){
                            model_compo.addRow(new Object[] {"Nothing Found"});

                        }
                        else {
                            model_compo.addRow(new Object[] {""});
                        }
                    }
                }

                rs.close();
                stmt.close();
                con.close();
                jTextField6.setText("");
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.printStackTrace();
            }

        }
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
        jButton13ActionPerformed(evt);
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
        if(jTextField5.getText().equals("")){
            JOptionPane.showMessageDialog(this, "Please fillup the input!!!");
        }
        else{
            String search_name=jTextField5.getText().toLowerCase();
            int stock=0;
            int mrp=0;
            String location="";
            String compo1="NA";
            String compo2="NA";
            String compo3="NA";
            String des="NA";
            String note="NA";
            Connection con; ResultSet rs;

            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();

                String query="select * from MED_DETAILS";
                rs = stmt.executeQuery(query);

                DefaultTableModel model_med = (DefaultTableModel)jTable11.getModel();

                model_med.setNumRows(0);

                int med_row_count=0;
                int compo_row_count=0;

                while(rs.next()){
                    if(rs.getString("NAME").toLowerCase().equals(search_name)){
                        compo1=rs.getString("CONFIG1");
                        compo2=rs.getString("CONFIG2");
                        compo3=rs.getString("CONFIG3");
                        des=rs.getString("DESCRIPTION");
                        note=rs.getString("NOTE");
                    }

                    if(rs.getString("NAME").toLowerCase().matches("(.*)"+search_name+"(.*)")){
                        boolean found=false;
                        for(int i=model_med.getRowCount()-1;i>=0;i--){
                            if(model_med.getValueAt(i, 0).equals(rs.getString("NAME"))){
                                found=true;
                            }
                        }
                        if(!found){
                            model_med.addRow(new Object[] {rs.getString("NAME")});
                        }
                    }
                }

                query="select * from MED_DETAILS";
                rs = stmt.executeQuery(query);

                while(rs.next()){
                    int match_count=0;
                    if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+compo1.toLowerCase()+"(.*)")
                        || rs.getString("CONFIG2").toLowerCase().matches("(.*)"+compo1.toLowerCase()+"(.*)")
                        || rs.getString("CONFIG3").toLowerCase().matches("(.*)"+compo1.toLowerCase()+"(.*)")){
                        match_count++;
                    }

                    if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+compo2.toLowerCase()+"(.*)")
                        || rs.getString("CONFIG2").toLowerCase().matches("(.*)"+compo2.toLowerCase()+"(.*)")
                        || rs.getString("CONFIG3").toLowerCase().matches("(.*)"+compo2.toLowerCase()+"(.*)")){
                        match_count++;
                    }

                    if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+compo3.toLowerCase()+"(.*)")
                        || rs.getString("CONFIG2").toLowerCase().matches("(.*)"+compo3.toLowerCase()+"(.*)")
                        || rs.getString("CONFIG3").toLowerCase().matches("(.*)"+compo3.toLowerCase()+"(.*)")){
                        match_count++;
                    }

                    if (match_count>0){
                        model_med.addRow(new Object[] {rs.getString("NAME")});
                    }
                }

                query="select * from STOCK";
                rs = stmt.executeQuery(query);
                boolean found_stock=false;
                while(rs.next()){
                    if(rs.getString("MED_NAME").toLowerCase().equals(search_name)){
                        stock=rs.getInt("QUANTITY");
                        mrp=rs.getInt("PRICE");
                        location=rs.getString("LOCATION");
                        found_stock=true;
                    }
                }
                int n=model_med.getRowCount();
                for(int i=0;i<n;i++){
                    String name=(String)model_med.getValueAt(i,0);

                    query="select * from STOCK";
                    rs = stmt.executeQuery(query);

                    while(rs.next()){
                        if(rs.getString("MED_NAME").toLowerCase().equals(name.toLowerCase())){
                            model_med.setValueAt("Available", i, 1);
                            jButton12.setVisible(false);
                        }
                        else {
                            model_med.setValueAt("Not Available", i, 1);
                            jButton12.setVisible(true);
                        }
                    }
                }

                if(found_stock){
                    jLabel73.setText("Available in stock!!!");
                    jLabel73.setForeground(Color.green);
                    jButton12.setVisible(false);
                }
                else{
                    jLabel73.setText("Not available in stock!!!");
                    jLabel73.setForeground(Color.red);
                    jButton12.setVisible(true);
                }

                jLabel74.setText(String.valueOf(stock));
                jLabel105.setText(String.valueOf(mrp));
                jLabel104.setText(location);
                jLabel80.setText(compo1+", "+compo2+" & "+compo3);
                jLabel106.setText("<html>"+des+"<html>");
                jLabel107.setText("<html>"+note+"<html>");

                if (model_med.getRowCount()<5){

                    for(int i=5-model_med.getRowCount();i>0;i--){
                        if (i==5){
                            model_med.addRow(new Object[] {"Nothing Found"});
                        }
                        else{
                            model_med.addRow(new Object[] {""});
                        }
                    }
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.printStackTrace();
            }

        }
    }//GEN-LAST:event_jButton13ActionPerformed
    
    private void shortlist(String name){
        
        Connection con; ResultSet rs;
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            con=(Connection)DriverManager.getConnection(connectionURL);
            Statement stmt=con.createStatement();

            String query="select * from SHORTLIST";
            rs = stmt.executeQuery(query);

            boolean search_found=false;
            int frequency=0;
            while(rs.next()){
                if(name.equals(rs.getString("NAME"))){
                    search_found=true;
                    frequency=rs.getInt("FREQUENCY");
                }
            }
            frequency++;
            
            
            rs=stmt.executeQuery("SELECT FREQUENCY FROM MED_DETAILS WHERE CAST(NAME AS VARCHAR(128))='"+name+"'");
            
            while(rs.next()){
                frequency=frequency+rs.getInt("FREQUENCY");
            }
            
            if(search_found){
                stmt.executeUpdate("DELETE FROM SHORTLIST WHERE NAME='"+name+"'");
            }

            Date date=new Date();
            stmt.executeUpdate("INSERT INTO SHORTLIST VALUES('"+name+"','"+new SimpleDateFormat("dd/MM/yyy").format(date)+"','"+new SimpleDateFormat("HH:mm:ss").format(date)+"',"+frequency+")");

            
                    JOptionPane.showMessageDialog(this, "'"+name+"' is shortlisted.");
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
    }
    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        String name=jTextField5.getText();
        shortlist(name);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
        jTextField4.requestFocus(true);
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
        jTextField3.requestFocus(true);
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
        jButton5.requestFocus();
        jButton5ActionPerformed(evt);
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        if(jTextField3.getText().equals("") && jTextField4.getText().equals("") && jTextField7.getText().equals("") ){
            JOptionPane.showMessageDialog(this, "Please fillup the input!!!");
        }
        else{
            String compo1=jTextField7.getText().toLowerCase();
            String compo2=jTextField4.getText().toLowerCase();
            String compo3=jTextField3.getText().toLowerCase();
            if (compo1.equals("")){
                compo1="$$@/./@#$";
            }
            if (compo2.equals("")){
                compo2="$$@/./@#$";
            }
            if (compo3.equals("")){
                compo3="$$@/./@#$";
            }
            Connection con; ResultSet rs;

            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                    System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                String query="select * from MED_DETAILS";
                rs = stmt.executeQuery(query);

                DefaultTableModel model_med = (DefaultTableModel)jTable5.getModel();
                DefaultTableModel model1 = (DefaultTableModel)jTable8.getModel();
                DefaultTableModel model2 = (DefaultTableModel)jTable9.getModel();
                DefaultTableModel model3 = (DefaultTableModel)jTable10.getModel();

                model_med.setNumRows(0);
                model1.setNumRows(0);
                model2.setNumRows(0);
                model3.setNumRows(0);

                int med_row_count=0;

                boolean found=true;
                while(rs.next()){
                    int match_count=0;
                    if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+compo1+"(.*)")
                        || rs.getString("CONFIG2").toLowerCase().matches("(.*)"+compo1+"(.*)")
                        || rs.getString("CONFIG3").toLowerCase().matches("(.*)"+compo1+"(.*)")){
                        match_count++;
                    }

                    if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+compo2+"(.*)")
                        || rs.getString("CONFIG2").toLowerCase().matches("(.*)"+compo2+"(.*)")
                        || rs.getString("CONFIG3").toLowerCase().matches("(.*)"+compo2+"(.*)")){
                        match_count++;
                    }

                    if(rs.getString("CONFIG1").toLowerCase().matches("(.*)"+compo3+"(.*)")
                        || rs.getString("CONFIG2").toLowerCase().matches("(.*)"+compo3+"(.*)")
                        || rs.getString("CONFIG3").toLowerCase().matches("(.*)"+compo3+"(.*)")){
                        match_count++;
                    }

                    if (match_count==3){
                        model1.addRow(new Object[] {rs.getString("NAME"), rs.getString("CONFIG1"), rs.getString("CONFIG2"), rs.getString("CONFIG3")});
                    }
                    else if (match_count==2){
                        model2.addRow(new Object[] {rs.getString("NAME"), rs.getString("CONFIG1"), rs.getString("CONFIG2"), rs.getString("CONFIG3")});
                    }
                    else if (match_count==1){
                        model3.addRow(new Object[] {rs.getString("NAME"), rs.getString("CONFIG1"), rs.getString("CONFIG2"), rs.getString("CONFIG3")});
                    }
                }

                for(int i=0;i<model1.getRowCount();i++){
                    model_med.addRow(new Object[] {model1.getValueAt(i, 0),model1.getValueAt(i, 1),model1.getValueAt(i, 2),model1.getValueAt(i, 3)});
                    med_row_count++;
                }

                for(int i=0;i<model2.getRowCount();i++){
                    model_med.addRow(new Object[] {model2.getValueAt(i, 0),model2.getValueAt(i, 1),model2.getValueAt(i, 2),model2.getValueAt(i, 3)});
                    med_row_count++;
                }

                for(int i=0;i<model3.getRowCount();i++){
                    model_med.addRow(new Object[] {model3.getValueAt(i, 0),model3.getValueAt(i, 1),model3.getValueAt(i, 2),model3.getValueAt(i, 3)});
                    med_row_count++;
                }

                if (med_row_count<10){
                    for(int i=10-med_row_count;i>0;i--){
                        if (i==10){
                            model_med.addRow(new Object[] {"Nothing","Found"});
                            found=false;
                        }
                        else{
                            model_med.addRow(new Object[] {""});
                        }
                    }
                }
                if (!found){
                    JOptionPane.showMessageDialog(this, "Sorry!!! There is no any medicine that matches your composition.");
                }
                rs.close();
                stmt.close();
                con.close();
                /*jTextField3.setText("");
                jTextField3.setText("");
                jTextField3.setText("");*/
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.printStackTrace();
            }

        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        if(jCheckBox1.isSelected()==true){
            jTextField20.setEnabled(true);
            jTextField10.setEnabled(true);
            jTextField9.setEnabled(true);
        }
        else if(jCheckBox1.isSelected()==false){
            jTextField20.setEnabled(false);
            jTextField10.setEnabled(false);
            jTextField9.setEnabled(false);
        }
        jTextField20.requestFocus(true);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jTextField20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField20ActionPerformed
        // TODO add your handling code here:
        jTextField10.requestFocus(true);
    }//GEN-LAST:event_jTextField20ActionPerformed

    private void jTextField14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField14ActionPerformed
        // TODO add your handling code here:
        jCheckBox1.requestFocus(true);
    }//GEN-LAST:event_jTextField14ActionPerformed

    private void jTextField13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField13ActionPerformed
        // TODO add your handling code here:
        jTextArea4.requestFocus(true);
    }//GEN-LAST:event_jTextField13ActionPerformed

    private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
        // TODO add your handling code here:
        jTextField13.requestFocus(true);
    }//GEN-LAST:event_jTextField12ActionPerformed

    private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
        // TODO add your handling code here:
        jTextField12.requestFocus(true);
    }//GEN-LAST:event_jTextField11ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed
        // TODO add your handling code here:
        jTextField9.requestFocus(true);
    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
        jTextField11.requestFocus(true);
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
        if(active){
            jTextField8.setText(null);
            jTabbedPane2.setSelectedIndex(5);
            jTabbedPane1.setSelectedIndex(0);
            
            jTextField6.setText(jTextField8.getText());
            jButton15ActionPerformed(evt);
            
            jTextField7.setText(jTextField8.getText());
            jButton5ActionPerformed(evt);
            
            jTextField5.setText(jTextField8.getText());
            jButton13ActionPerformed(evt);
            
        }else{
            registerMsg();
            jTextField8.setText(null);
            jTextField8.requestFocus(false);
        }
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jTextField23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField23ActionPerformed
        // TODO add your handling code here:
        if(active){
            shortlist(jTextField23.getText());
            jTextField23.setText("");
        }else{
            registerMsg();
            jTextField23.setText("");
            jTextField23.requestFocus(false);
        }
    }//GEN-LAST:event_jTextField23ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        // TODO add your handling code here:
        if(!jTextField17.getText().equals("")){
            Connection con; ResultSet rs;
            String search_name=jTextField17.getText();
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                String query="";
                rs = stmt.executeQuery("select * from STOCK");

                boolean search_found=false;
                while(rs.next()){
                    if(rs.getString("MED_NAME").equals(search_name)){
                        jLabel82.setText(rs.getString("QUANTITY"));
                        jLabel86.setText(rs.getString("PRICE"));
                        jLabel173.setText(rs.getString("LOCATION"));
                        search_found=true;
                        jLabel1.setText("Available in stock!!!");
                        jLabel1.setForeground(Color.green);
                        jTextField21.requestFocus(true);
                    }
                }
                
                if(search_found){
                    rs=stmt.executeQuery("SELECT * FROM MED_DETAILS");
                    while(rs.next()){
                        if(search_name.equals(rs.getString("NAME"))){
                            jEditorPane3.setText(rs.getString("CONFIG1")+", "+rs.getString("CONFIG2")+" & "+rs.getString("CONFIG3"));
                            jEditorPane4.setText(rs.getString("DESCRIPTION"));
                            jEditorPane5.setText(rs.getString("NOTE"));
                        }
                    }
                }
                else {
                    jLabel1.setText("Not available in stock!!!");
                    jLabel1.setForeground(Color.red);    
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Please enter a medicine name!!!");
        }
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jTextField17FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField17FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField17FocusGained

    private void jTextField21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField21ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField21ActionPerformed

    private void jTextField21KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField21KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField21KeyTyped

    private void jTextField21KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField21KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField21KeyPressed

    private void jTextField21KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField21KeyReleased
        // TODO add your handling code here:
        if(jTextField21.getText().equals("")){
            jTextField21.setText("0");
        }
        if(!jTextField17.getText().equals("") && Double.valueOf(jTextField21.getText())>=0){
            
            Connection con; ResultSet rs;
            String search_name=jTextField17.getText();
            Double price=0.0;
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                rs = stmt.executeQuery("select * from STOCK");

                if (jTextField18.getText().equals("")){
                    while(rs.next()){
                        if(rs.getString("MED_NAME").equals(search_name)){
                            price=rs.getDouble("PRICE");
                        }
                    }
                }
                else if(Double.valueOf(jTextField18.getText())>=0){
                    price=Double.valueOf(jTextField18.getText());
                }
                else{
                    JOptionPane.showMessageDialog(this, "Please enter a proper input for price!!!");
                }
                
                jLabel87.setText(String.valueOf(price*Double.valueOf(jTextField21.getText())));
                
                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
        else{
            if(jTextField17.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please enter medicine name!!!");
            }
            else if(Double.valueOf(jTextField21.getText())<=0) {
                JOptionPane.showMessageDialog(this, "Quantity field should be filled with valid input!!!");
            }
            else{
                JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            }
        }
    }//GEN-LAST:event_jTextField21KeyReleased

    private void jTextField18KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField18KeyReleased
        // TODO add your handling code here:
        if(!jTextField17.getText().equals("") && !jTextField21.getText().equals("") && Double.valueOf(jTextField21.getText())>0){
            if(jTextField18.getText().equals("")){
                jTextField18.setText("0"); 
            }
            Double price=Double.valueOf(jTextField18.getText());
                
            jLabel87.setText(String.valueOf(price*Double.valueOf(jTextField21.getText())));
            
        }
        else{
            if(jTextField17.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please enter medicine name!!!");
            }
            else if(jTextField21.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Quantity field shouldn't be vacant!!!");
            }
            else if(Double.valueOf(jTextField21.getText())<=0) {
                JOptionPane.showMessageDialog(this, "Quantity field should be filled with valid input!!!");
            }
            else{
                JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            }
        }
    }//GEN-LAST:event_jTextField18KeyReleased

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        // TODO add your handling code here:
                jButton20ActionPerformed(evt);
        if(!jTextField17.getText().equals("") && !jTextField21.getText().equals("") && Double.valueOf(jTextField21.getText())>0){
            
            Connection con; ResultSet rs;
            String search_name=jTextField17.getText();
            Double price=0.0;
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                rs = stmt.executeQuery("select * from STOCK");

                boolean search_found=false;
                int quantity=0;
                while(rs.next()){
                    if(rs.getString("MED_NAME").equals(search_name)){
                        price=rs.getDouble("PRICE");
                        search_found=true;
                        quantity = rs.getInt("QUANTITY")-Integer.valueOf(jTextField21.getText());
                    }
                }
                
                if(search_found){
                    if(!jTextField18.getText().equals("")){
                        if(Double.valueOf(jTextField18.getText())>=0){
                            price=Double.valueOf(jTextField18.getText());
                        }
                        else{
                            JOptionPane.showMessageDialog(this, "You have entered an invalid input. The amount has been calculated according to the saved details.");
                        }
                    }
                    
                    Double amount=price*Double.valueOf(jTextField21.getText());
                    jLabel87.setText(String.valueOf(amount));

                    
                    if(quantity>=0){
                        DefaultTableModel model = (DefaultTableModel)jTable12.getModel();
                        model.addRow(new Object[] {search_name,Integer.valueOf(jTextField21.getText()),price,amount});
                        jLabel116.setText(String.valueOf(Double.valueOf(jLabel87.getText())+Double.valueOf(jLabel116.getText())));

                        jTextField21.setText("");
                        jTextField18.setText("");
                        jTextField17.setText("");
                        jLabel82.setText("0");
                        jLabel86.setText("0.0");
                        jLabel173.setText("NA");
                        jEditorPane3.setText("");
                        jEditorPane4.setText("");
                        jEditorPane5.setText("");
                        jLabel87.setText("0.0");
                    }
                    else{
                        JOptionPane.showMessageDialog(this, "The required amount of medicine is not available in your stock");
                        jTextField21.requestFocus(true);
                    }
                    
                    
                }
                else {
                    jLabel1.setText("Not available in stock!!!");
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
        else{
            
            if(jTextField17.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please enter medicine name!!!");
            }
            else if(jTextField21.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Quantity field shouldn't be vacant!!!");
            }
            else if(Double.valueOf(jTextField21.getText())<=0) {
                JOptionPane.showMessageDialog(this, "Quantity field should be filled with valid input!!!");
            }
            else{
                JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            }
        }
    }//GEN-LAST:event_jButton19ActionPerformed
    
    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model =(DefaultTableModel)jTable12.getModel();
        model.setNumRows(0);
        jLabel116.setText("0");
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jTextField24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField24ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField24ActionPerformed

    private void jComboBox4KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBox4KeyReleased
        // TODO add your handling code here:            
    }//GEN-LAST:event_jComboBox4KeyReleased

    private void jLabel198MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel198MouseClicked
        // TODO add your handling code here:
        if(!jEditorPane6.getText().equals("") && !jTextField25.getText().equals("")){
            Connection con; ResultSet rs;
            Date myDate = new Date();
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();
                
                rs = stmt.executeQuery("select NAME from NOTE");
                
                boolean search_found=false;
                while(rs.next()){
                    if(jTextField25.getText().equals(rs.getString("NAME"))){
                        search_found=true;
                    }
                }
                
                
                String options[]={"YES, OVERWRITE","NO"};
                if(search_found){
                    if(JOptionPane.showOptionDialog(null, "A note is already saved with '"+jTextField25.getText()+"'.\nPlease change the name, else it will be overwritten.","Warning",
                        JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0])==JOptionPane.YES_OPTION){
                        stmt.executeUpdate("DELETE FROM NOTE WHERE NAME=('"+jTextField25.getText()+"')");
                        stmt.executeUpdate("INSERT INTO NOTE VALUES('"+jTextField25.getText()+"','"+jEditorPane6.getText()+"','"+new SimpleDateFormat("dd/MM/yyyy").format(myDate)+"')");
                        
                        jTextField25.setText("");
                        jEditorPane6.setText("");

                        JOptionPane.showMessageDialog(this, "Your note has been overwritten!!!\nYou can reference your notes by visiting the same page.");
                    }
                }
                else{
                    stmt.executeUpdate("INSERT INTO NOTE VALUES('"+jTextField25.getText()+"','"+jEditorPane6.getText()+"','"+new SimpleDateFormat("dd/MM/yyyy").format(myDate)+"')");
                    jTextField25.setText("");
                    jEditorPane6.setText("");

                    JOptionPane.showMessageDialog(this, "Your note has been saved successfully!!!\nYou can reference your notes by visiting the same page.");
                }
                
                rs = stmt.executeQuery("select NAME from NOTE");

                while(rs.next()){
                    jComboBox4.addItem(rs.getString("NAME"));
                }

                
                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }   
        }
        else {
            if(jTextField25.getText().equals("") && !jEditorPane6.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please give a name to your note!!!");
            }
            else if(jEditorPane6.getText().equals("") && !jTextField25.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please write some contents in your note!!!");
            }
            else {
                JOptionPane.showMessageDialog(this, "Please give a name and write some contents!!!");
            }
        }
    }//GEN-LAST:event_jLabel198MouseClicked

    private void jLabel193MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel193MouseClicked
        // TODO add your handling code here:
        jEditorPane6.setText("");
    }//GEN-LAST:event_jLabel193MouseClicked

    private void jLabel192MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel192MouseClicked
        // TODO add your handling code here:
        String options[]={"YES, DELETE IT","NO, GET BACK"};
        if(JOptionPane.showOptionDialog(null, "Do you really want to delete this note?","Warning",
                        JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0])==JOptionPane.YES_OPTION){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                Statement stmt=con.createStatement();
                stmt.executeUpdate("DELETE FROM NOTE WHERE NAME='"+jTextField25.getText()+"'");    
                jTextField25.setText("");
                jEditorPane6.setText("");
                
                
                ResultSet rs = stmt.executeQuery("select NAME from NOTE");

                while(rs.next()){
                    jComboBox4.addItem(rs.getString("NAME"));
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
    }//GEN-LAST:event_jLabel192MouseClicked

    private void jLabel194MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel194MouseClicked
        // TODO add your handling code here:
         if(!jEditorPane6.getText().equals("") && !jTextField25.getText().equals("")){
            Connection con; ResultSet rs;
            Date myDate = new Date();
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                
                rs = stmt.executeQuery("select NAME from NOTE");
                
                boolean search_found=false;
                while(rs.next()){
                    if(jTextField25.getText().equals(rs.getString("NAME"))){
                        search_found=true;
                    }
                }
                if(search_found){
                    stmt.executeUpdate("DELETE FROM NOTE WHERE NAME='"+jTextField25.getText()+"'");
                }
                
                stmt.executeUpdate("INSERT INTO NOTE VALUES('"+jTextField25.getText()+"','"+jEditorPane6.getText()+"','"+new SimpleDateFormat("dd/MM/yyyy").format(myDate)+"')");
                
                jTextField25.setText("");
                jEditorPane6.setText("");
                
                JOptionPane.showMessageDialog(this, "Your note has been updated successfully!!!\nYou can reference your notes by visiting the same page.");
                
                
                rs = stmt.executeQuery("select NAME from NOTE");

                while(rs.next()){
                    jComboBox4.addItem(rs.getString("NAME"));
                }

                
                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }   
        }
        else {
            if(jTextField25.getText().equals("") && !jEditorPane6.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please give a name to your note!!!");
            }
            else if(jEditorPane6.getText().equals("") && !jTextField25.getText().equals("")){
                JOptionPane.showMessageDialog(this, "Please write some contents in your note!!!");
            }
            else {
                JOptionPane.showMessageDialog(this, "Please give a name and write some contents!!!");
            }
        }
    }//GEN-LAST:event_jLabel194MouseClicked

    private void jComboBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox4ItemStateChanged
        // TODO add your handling code here:
         Connection con; ResultSet rs;
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                rs = stmt.executeQuery("SELECT * FROM NOTE");

                while(rs.next()){
                    if(jComboBox4.getSelectedItem().equals(rs.getString("NAME"))){
                        jEditorPane6.setText(rs.getString("CONTENT"));
                        jTextField25.setText(rs.getString("NAME"));
                        jLabel196.setText(rs.getString("DATE"));
                    }
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
    }//GEN-LAST:event_jComboBox4ItemStateChanged

    private void jLabel189MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel189MouseClicked
        // TODO add your handling code here:
        if(!jTextField24.getText().equals("")){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                System.out.println("Connecttion SuccessFul");

                Statement stmt=con.createStatement();

                ResultSet rs = stmt.executeQuery("select NAME from SHORTLIST");

                DefaultTableModel model=(DefaultTableModel)jTable16.getModel();
                boolean search_found=false;
                while(rs.next()){
                    if(rs.getString("NAME").equals(jTextField24.getText())){
                        search_found=true;
                    }
                }
                if(search_found){
                    stmt.executeUpdate("DELETE FROM SHORTLIST WHERE NAME=('"+jTextField24.getText()+"')");
                    model.setRowCount(0);
                    rs=stmt.executeQuery("SELECT * FROM SHORTLIST");
                    while(rs.next()){
                        model.addRow(new Object[] {rs.getString("NAME"),rs.getString("DATE"),rs.getString("TIME")});
                    }
                    jTextField24.setText("");
                }
                else{
                    JOptionPane.showMessageDialog(this, "There is no item in your shortlist named as '"+jTextField24.getText()+"'.");
                    jTextField24.requestFocus(true);
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.getStackTrace();
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Please input a name of item to enlist or delist it!!!");
        }
    }//GEN-LAST:event_jLabel189MouseClicked

    private void jLabel188MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel188MouseClicked
        // TODO add your handling code here:
        if(!jTextField24.getText().equals("")){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                Date myDate=new Date();
                ResultSet rs = stmt.executeQuery("select NAME from SHORTLIST");

                DefaultTableModel model=(DefaultTableModel)jTable16.getModel();
                boolean search_found=false;
                while(rs.next()){
                    if(rs.getString("NAME").equals(jTextField24.getText())){
                        search_found=true;
                    }
                }
                if(!search_found){
                    stmt.executeUpdate("INSERT INTO SHORTLIST VALUES('"+jTextField24.getText()+"','"+new SimpleDateFormat("dd/MM/yyyy").format(myDate)+"','"+new SimpleDateFormat("HH:mm:ss").format(myDate)+"',0)");
                    model.setRowCount(0);
                    rs=stmt.executeQuery("SELECT * FROM SHORTLIST");
                    while(rs.next()){
                        model.addRow(new Object[] {rs.getString("NAME"),rs.getString("DATE"),rs.getString("TIME")});
                    }
                    jTextField24.setText("");
                }
                else{
                    JOptionPane.showMessageDialog(this, "The item is already shortlisted.");
                    jTextField24.requestFocus(true);
                }

                rs.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.getStackTrace();
            }
        }
        else{
            JOptionPane.showMessageDialog(this, "Please input a name of item to enlist it!!!");
        }
    }//GEN-LAST:event_jLabel188MouseClicked

    private void jLabel199MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel199MouseClicked
        // TODO add your handling code here:
        String options[]={"YES, DELETE","NO"};
        if(JOptionPane.showOptionDialog(null, "Please note that you will not be able to recover this shortlist again in future.","Warning",
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0])==JOptionPane.YES_OPTION){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();

                stmt.executeUpdate("TRUNCATE TABLE SHORTLIST");
                
                DefaultTableModel model=(DefaultTableModel)jTable16.getModel();
                model.setRowCount(0);
                jTextField24.setText("");

                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.getStackTrace();
            }
        }
    }//GEN-LAST:event_jLabel199MouseClicked

    private void jLabel154MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel154MouseClicked
        // TODO add your handling code here:
        try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Date date=jXDatePicker1.getDate();
                String table_name="DAY" + new SimpleDateFormat("ddMMyyyy").format(date);
                DefaultTableModel model=(DefaultTableModel)jTable15.getModel();
                
                
                Statement stmt=con.createStatement();
                DatabaseMetaData dbm = con.getMetaData();
                
                ResultSet tables = dbm.getTables(null, null, table_name, null);
                boolean table_found=false;
                if (tables.next()) {
                  table_found=true;
                }
                else {
                    JOptionPane.showMessageDialog(this, "There is no any sales detail of this day.");
                }
                
                if(table_found){
                    model.setRowCount(0);
                    ResultSet rs=stmt.executeQuery("SELECT * FROM "+table_name);
                    while(rs.next()){
                        String name=rs.getString("NAME");
                        int quantity=rs.getInt("QUANTITY");
                        double price=rs.getDouble("PRICE");
                        String time=rs.getString("TIME");
                        String date1=rs.getString("DATE");
                        
                        model.addRow(new Object[]  {name,quantity,price,time,date1});
                    }
                    rs.close();
                }
                
                update_item_table();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.getStackTrace();
            }
    }//GEN-LAST:event_jLabel154MouseClicked

    private void jXDatePicker1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXDatePicker1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jXDatePicker1ActionPerformed

    private void jLabel155MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel155MouseClicked
        // TODO add your handling code here:

        String post_name="";
        if((int)jComboBox1.getSelectedIndex()<9){
            post_name="0"+String.valueOf(jComboBox1.getSelectedIndex()+1)+(String)jComboBox2.getSelectedItem();
        }
        else{
            post_name=String.valueOf(jComboBox1.getSelectedIndex()+1)+(String)jComboBox2.getSelectedItem();
        }
        
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
            
            DefaultTableModel model=(DefaultTableModel)jTable15.getModel();

            Statement stmt=con.createStatement();
            DatabaseMetaData dbm = con.getMetaData();
            String table_name="";
            boolean data_found=false;
            for(int i=1;i<32;i++){
                if(i<10){
                    table_name="DAY0"+String.valueOf(i)+post_name;
                }else{
                    table_name="DAY"+String.valueOf(i)+post_name;
                }
                
                System.out.println(table_name);
                
                ResultSet tables = dbm.getTables(null, null, table_name, null);
                boolean table_present=false;
                if(tables.next()) {
                    table_present=true;
                }
                if(table_present){
                    if(!data_found){
                        model.setRowCount(0);
                    }
                    data_found=true;
                    ResultSet rs=stmt.executeQuery("SELECT * FROM "+table_name);
                    while(rs.next()){
                        model.addRow(new Object[] {rs.getString("NAME"),rs.getInt("QUANTITY"),rs.getDouble("PRICE"),rs.getString("TIME"),rs.getString("DATE")});
                    }
                }
            }
            if(!data_found){
                JOptionPane.showMessageDialog(this, "No saved data found for this month.");
            }
            update_item_table();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.getStackTrace();
        }
    }//GEN-LAST:event_jLabel155MouseClicked

    private void jLabel156MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel156MouseClicked
        // TODO add your handling code here:
        
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
            
            DefaultTableModel model=(DefaultTableModel)jTable15.getModel();
            DefaultComboBoxModel model1=(DefaultComboBoxModel)jComboBox5.getModel();

            Statement stmt=con.createStatement();
            DatabaseMetaData dbm = con.getMetaData();
            
            ResultSet tables = dbm.getTables(null, null, "%", null);
            boolean table_found=false;
            
            while(tables.next()) {
                String name=tables.getString(3);
                if(name.substring(name.length()-4, name.length()).equals(jComboBox3.getSelectedItem())){
                    model1.addElement(name);
                    table_found=true;
                }
            }
            
            if(table_found){
                model.setNumRows(0);
                for (int i=0;i<model1.getSize();i++){

                    String name=(String)model1.getElementAt(i);
                    ResultSet rs=stmt.executeQuery("SELECT * FROM "+name);
                    while(rs.next()){
                        model.addRow(new Object[] {rs.getString("NAME"),rs.getInt("QUANTITY"),rs.getDouble("PRICE"),rs.getString("TIME"),rs.getString("DATE")});
                    }
                }
                update_item_table();
            }
            else{
                JOptionPane.showMessageDialog(this, "No saved data found for this year.");
            }
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.getStackTrace();
        }
    }//GEN-LAST:event_jLabel156MouseClicked

    private void jLabel203MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel203MouseClicked
        // TODO add your handling code here:
            jTabbedPane2.setSelectedIndex(0);
    refresh_jTable2();
        refresh_jTable3();
    
    }//GEN-LAST:event_jLabel203MouseClicked

    private void jLabel207MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel207MouseClicked
        // TODO add your handling code here:
        if(active){
            jTabbedPane2.setSelectedIndex(3);
        }
        else{
            registerMsg();
        }
    }//GEN-LAST:event_jLabel207MouseClicked

    public String post(String url, Map<String, String> params) {

        //Check if Valid URL
        if(!url.toLowerCase().contains("http://")) return null;

        StringBuilder bldr = new StringBuilder();

        try {
            //Build the post data
            StringBuilder post_data = new StringBuilder();

            //Build the posting variables from the map given
            for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                String value = (String)entry.getValue();

                if(key.length() > 0 && value.length() > 0) {

                    if(post_data.length() > 0) post_data.append("&");

                    post_data.append(URLEncoder.encode(key, "UTF-8"));
                    post_data.append("=");
                    post_data.append(URLEncoder.encode(value, "UTF-8"));
                }
            }

            // Send data
            URL remote_url = new URL(url);
            URLConnection conn = remote_url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(post_data.toString());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = rd.readLine()) != null) {
                bldr.append(inputLine);
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            //Handle Error
        }

        return bldr.length() > 0 ? bldr.toString() : null;
    }
    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        // TODO add your handling code here:
        String old=String.valueOf(jPasswordField3.getPassword());
        String new1=String.valueOf(jPasswordField4.getPassword());
        String new2=String.valueOf(jPasswordField5.getPassword());

        if(!old.equals("") && !new1.equals("") && !new2.equals("") && !old.equals(new1) && new2.equals(new1)){
            Connection con; ResultSet rs;
            
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                boolean update=false;
                
                rs=stmt.executeQuery("SELECT PASSWORD FROM STOCKUP_ADMIN");
                
                while(rs.next()){
                    if(rs.getString("PASSWORD").equals(old)){
                        update=true;
                    }
                }
                
                if(update){
                    stmt.executeUpdate("UPDATE STOCKUP_ADMIN SET PASSWORD='"+new1+"' WHERE PASSWORD='"+old+"'");
                    jPasswordField3.setText("");
                    jPasswordField4.setText("");
                    jPasswordField5.setText("");
                    JOptionPane.showMessageDialog(this, "Password changed succesfully!!!");
                    
                }
                else{
                    JOptionPane.showMessageDialog(this, "Please Enter the correct password!!!");
                    jPasswordField3.requestFocus(true);
                    jPasswordField3.setText("");
                }
                           
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
        else if(old.equals("")){
            JOptionPane.showMessageDialog(this, "Please enter the current password!!!");
        }
        else if(new1.equals("")){
            JOptionPane.showMessageDialog(this, "Please enter the new password!!!");
        }
        else if(new2.equals("")){
            JOptionPane.showMessageDialog(this, "Please retype the new password!!!");
        }
        else if(!new2.equals(new1)){
            JOptionPane.showMessageDialog(this, "Please make sure that you have retyped the new password correctly!!!");
        }
        else if(old.equals(new1)){
            JOptionPane.showMessageDialog(this, "Please enter a new password other than previouse one");
        }
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jLabel212MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel212MouseClicked
        // TODO add your handling code here:
        Object options[]={"YES, DELETE","NO, LEAVE"};
        if(JOptionPane.showOptionDialog(null, "Are you sure, you want to delete the today's sales details?","Warning",
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0])==JOptionPane.YES_OPTION){
            
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                stmt.executeUpdate("TRUNCATE TABLE TODAY_SALE");
                JOptionPane.showMessageDialog(this, "Details have been cleared");
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }            
            
        }
    }//GEN-LAST:event_jLabel212MouseClicked

    private void jLabel213MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel213MouseClicked
        // TODO add your handling code here:
        Object options[]={"YES, DELETE","NO, LEAVE"};
        if(JOptionPane.showOptionDialog(null, "Are you sure, you want to delete the LastDay sales details?","Warning",
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0])==JOptionPane.YES_OPTION){
            
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                stmt.executeUpdate("TRUNCATE TABLE LASTDAY_SALE");
                JOptionPane.showMessageDialog(this, "Details have been cleared");
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
        }
    }//GEN-LAST:event_jLabel213MouseClicked

    private void jLabel214MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel214MouseClicked
        // TODO add your handling code here:
        
        Object options[]={"YES, DELETE","NO, LEAVE"};
        if(JOptionPane.showOptionDialog(null, "Are you sure, you want to delete the complete details?\nYou will not be able to recover these details again.","Warning",
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0])==JOptionPane.YES_OPTION){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                Statement stmt=con.createStatement();
                String table_name="DAY" + new SimpleDateFormat("ddMMyyyy").format(jXDatePicker2.getDate());
                stmt.executeUpdate("DROP TABLE "+table_name);
                JOptionPane.showMessageDialog(this, "Details have been cleared");
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }   
        }
    }//GEN-LAST:event_jLabel214MouseClicked

    private void jLabel215MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel215MouseClicked
        // TODO add your handling code here:
        
        Object options[]={"YES, DELETE","NO, LEAVE"};
        if(JOptionPane.showOptionDialog(null, "Are you sure, you want to delete the complete details of this month?\nYou will not be able to recover these details again.","Warning",
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0])==JOptionPane.YES_OPTION){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                Statement stmt=con.createStatement();
                String sub_name=(String)jComboBox7.getSelectedItem()+(String)jComboBox8.getSelectedItem();
                
                DefaultComboBoxModel model=(DefaultComboBoxModel)jComboBox5.getModel();

                DatabaseMetaData dbm = con.getMetaData();

                ResultSet tables = dbm.getTables(null, null, "%", null);
                
                boolean table_found=false;

                while(tables.next()) {
                    String name=tables.getString(3);
                    if(name.substring(name.length()-6, name.length()).equals(sub_name)){
                        model.addElement(name);
                        table_found=true;
                    }
                }

                if(table_found){
                    for (int i=0;i<model.getSize();i++){
                        String name=(String)model.getElementAt(i);
                        stmt.executeUpdate("DROP TABLE "+name);
                    }
                    JOptionPane.showMessageDialog(this, "Details have been cleared succesfully.");
                }
                else{
                    JOptionPane.showMessageDialog(this, "No saved data found for this month.");
                }

                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }   
        }
    }//GEN-LAST:event_jLabel215MouseClicked

    private void jLabel216MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel216MouseClicked
        // TODO add your handling code here:
        
        Object options[]={"YES, DELETE","NO, LEAVE"};
        if(JOptionPane.showOptionDialog(null, "Are you sure, you want to delete the complete details of this year?\nYou will not be able to recover these details again.","Warning",
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0])==JOptionPane.YES_OPTION){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                Statement stmt=con.createStatement();
                
                String sub_name=(String)jComboBox9.getSelectedItem();
                
                DefaultComboBoxModel model=(DefaultComboBoxModel)jComboBox5.getModel();

                DatabaseMetaData dbm = con.getMetaData();

                ResultSet tables = dbm.getTables(null, null, "%", null);
                
                boolean table_found=false;

                while(tables.next()) {
                    String name=tables.getString(3);
                    if(name.substring(name.length()-4, name.length()).equals(sub_name)){
                        model.addElement(name);
                        table_found=true;
                    }
                }

                if(table_found){
                    for (int i=0;i<model.getSize();i++){
                        String name=(String)model.getElementAt(i);
                        stmt.executeUpdate("DROP TABLE "+name);
                    }
                    JOptionPane.showMessageDialog(this, "Details have been cleared succesfully.");
                }
                else{
                    JOptionPane.showMessageDialog(this, "No saved data found for this month.");
                }

                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }   
        }
    }//GEN-LAST:event_jLabel216MouseClicked
public static String generatePassword(int length){
String alphabet = 
        new String("0123456789"); //9
int n = alphabet.length(); //10

String result = new String(); 
Random r = new Random(); //11

for (int i=0; i<length; i++) //12
    result = result + alphabet.charAt(r.nextInt(n)); //13

return result;
}
    private void jLabel217MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel217MouseClicked
        // TODO add your handling code here:
        jLabel217.setBackground(Color.white);
        jLabel217.setForeground(new Color(55,150,198));
        jLabel217.setText("Changing password...");
        jLabel217.setIcon(new ImageIcon(getClass().getResource("ring (2).gif")));
        new changePass().start();
    }//GEN-LAST:event_jLabel217MouseClicked

    private boolean changePassword(){
        boolean k=true;
        String new_pass=generatePassword(8);
        String userName="";
        String userMail="";
        String productKey="";
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            Connection con=(Connection)DriverManager.getConnection(connectionURL);
            Statement stmt=con.createStatement();

            ResultSet rs=stmt.executeQuery("SELECT * FROM STOCKUP_ADMIN");
            while(rs.next()){
                userName=rs.getString("USERNAME");
                userMail=rs.getString("USERMAIL");
                productKey=rs.getString("PRODUCTKEY");
            }
            
           
            k=sendMail(new_pass,userName,userMail,productKey);
            if(k){
                stmt.executeUpdate("UPDATE STOCKUP_ADMIN SET PASSWORD='"+new_pass+"'");

                jLabel217.setForeground(Color.white);
                jLabel217.setBackground(new Color(55,150,198));
                jLabel217.setText("Forgotten your password?");
                jLabel217.setIcon(null);
                JOptionPane.showMessageDialog(this, "The new password has been mailed to your email: '"+userMail+"'.");
            }
            else{
                JOptionPane.showMessageDialog(this,"Password change failed miserably!!!\nPlease contact our customer cell.");
            }
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }   

        jLabel217.setForeground(Color.white);
        jLabel217.setBackground(new Color(55,150,198));
        jLabel217.setText("Forgotten your password?");
        jLabel217.setIcon(null);
        return k;
    }         
    
    private boolean sendMail(String new_pass,String userName,String userMail,String productKey){
        boolean k=false;
        try {
            URL url = new URL("http://sybero.in/medicellar/registration/changePass.php");
            
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            
            PrintStream ps = new PrintStream(con.getOutputStream());
            ps.print("password="+new_pass);
            ps.print("&userName="+userName);
            ps.print("&userMail="+userMail);
            ps.print("&productKey="+productKey);
            
            
            // we have to get the input stream in order to actually send the request
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            String res = null;
            while ((line = in.readLine()) != null) {
                System.out.println("password change: "+line);
                res+=line;
            }
            res=res.substring(res.length()-7, res.length());
            if(res.equals("success")){
                k=true;
            }else{
                k=false;
            }
            // close the print stream
            ps.close();
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
            connectionMsg();
        }
        catch (IOException e) {
            e.printStackTrace();
            connectionMsg();
        }
        return k;
    }
    private void jLabel232MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel232MouseClicked
        // TODO add your handling code here:
        jTabbedPane6.setSelectedIndex(1);
        new buyKey().start();
        
    }//GEN-LAST:event_jLabel232MouseClicked
    public static boolean internetReachable(){
        try {
            //make a URL to a known source
            URL url = new URL("http://www.google.com");

            //open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            Object objData = urlConnect.getContent();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private void jLabel233MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel233MouseClicked
        // TODO add your handling code here:
        jTabbedPane5.setSelectedIndex(2);
        new registerPage().start();
    }//GEN-LAST:event_jLabel233MouseClicked

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jLabel242MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel242MouseClicked
        // TODO add your handling code here:
        try{     
                Desktop.getDesktop().browse(new URI("file:///C:/Program%20Files%20(x86)/Sybero%20Infotech/MediCellar/MediCellar-TermsOfUse.pdf"));   
            }catch(Exception e){
                e.getStackTrace();
                try{
                Desktop.getDesktop().browse(new URI("http://sybero.in/medicellar/docs/MediCellar-TermsOfUse.pdf"));   
                }
                catch(Exception err){
                    connectionMsg();
                }
            }
    }//GEN-LAST:event_jLabel242MouseClicked

        private String sn = null;
	private String getSerialNumber() {
            if (sn != null) {
                    return sn;
            }
            OutputStream os = null;
            InputStream is = null;
            Runtime runtime = Runtime.getRuntime();
            Process process = null;
            try {
                    process = runtime.exec(new String[] { "wmic", "bios", "get", "serialnumber" });
            } catch (IOException e) {
                    throw new RuntimeException(e);
            }

            os = process.getOutputStream();
            is = process.getInputStream();
            try {
                    os.close();
            } catch (IOException e) {
                    throw new RuntimeException(e);
            }

            Scanner sc = new Scanner(is);
            try {
                while (sc.hasNext()) {
                    String next = sc.next();
                    if ("SerialNumber".equals(next)) {
                        sn = sc.next().trim();
                        break;
                    }
                }
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (sn == null) {
                    throw new RuntimeException("Cannot find computer SN");
            }

            return sn;
	}
    
    private String getSysDetails(String add){
        InetAddress ip;
        String macAdd="";
        String ipAdd="";
	try {
		ip = InetAddress.getLocalHost();
		System.out.println("Current IP address : " + ip.getHostAddress());
		ipAdd=ip.getHostAddress();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			
		byte[] mac = network.getHardwareAddress();
			
		
                System.out.print("Current MAC address : ");
			
		
                StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
		}
		macAdd=sb.toString();
                System.out.println(macAdd);
			
	} 
        catch (UnknownHostException e) {
		e.printStackTrace();
                connectionMsg();
	} 
        catch (SocketException e){
		e.printStackTrace();
                connectionMsg();
	}
        String rslt="";
        if(add.equals("mac")){
            rslt=macAdd;
        }
        else if(add.equals("ip")){
            rslt=ipAdd;
        }
        return(rslt);
    }
    
    private boolean keyAvailable(String productKey){
        boolean k=true;
        try {
            URL url = new URL("http://sybero.in/medicellar/registration/keyAvailable.php");
            
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            
            PrintStream ps = new PrintStream(con.getOutputStream());
            ps.print("productKey="+productKey);
            
            // we have to get the input stream in order to actually send the request
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            String res = null;
            while ((line = in.readLine()) != null) {
                System.out.println("key available: "+line);
                res+=line;
            }
            res=res.substring(res.length()-7, res.length());
            if(res.equals("success")){
                k=true;
            }else{
                k=false;
            }
            // close the print stream
            ps.close();
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
            connectionMsg();
        } 
        catch (IOException e) {
            e.printStackTrace();
            connectionMsg();
        }
        return k;
    }
    
    private boolean keyRegistered(String productKey){
        boolean k=true;
        try {
            URL url = new URL("http://sybero.in/medicellar/registration/keyRegistered.php");
            
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            
            PrintStream ps = new PrintStream(con.getOutputStream());
            ps.print("productKey="+productKey);
            
            // we have to get the input stream in order to actually send the request
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            String res = null;
            while ((line = in.readLine()) != null) {
                System.out.println("key registered report: "+line);
                res+=line;
            }
            res=res.substring(res.length()-7, res.length());
            if(res.equals("success")){
                k=true;
            }else{
                k=false;
            }
            // close the print stream
            ps.close();
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
            connectionMsg();
        } 
        catch (IOException e) {
            e.printStackTrace();
            connectionMsg();
        }
        
        return k;
    }
    
    private boolean matchMacIp(String mac,String ip,String productKey){
        boolean k=true;
        try {
            URL url = new URL("http://sybero.in/medicellar/registration/matchMacIp.php");
            
            URLConnection con = url.openConnection();
            con.setDoOutput(true);
            
            PrintStream ps = new PrintStream(con.getOutputStream());
            ps.print("mac="+mac);
            ps.print("&ip="+ip);
            ps.print("&productKey="+productKey);
            
            // we have to get the input stream in order to actually send the request
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            String res = null;
            while ((line = in.readLine()) != null) {
                System.out.println("match mac or ip: "+line);
                res+=line;
            }
            res=res.substring(res.length()-7, res.length());
            if(res.equals("success")){
                k=true;
            }else{
                k=false;
            }
            // close the print stream
            ps.close();
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
            connectionMsg();
        } 
        catch (IOException e) {
            e.printStackTrace();
            connectionMsg();
        }
        
        return k;
    }
    private boolean activateAccount(int i,String mac,String ip,String productKey,String userMail,String userName,String password,String contact,String productId){
        boolean k=false;
        //i=1 is for new accnt
        try {
            // open a connection to the site
            String url_string="";
            if(i==1){
                url_string="http://sybero.in/medicellar/registration/addUser.php";
            }
            else if(i==0){
                url_string="http://sybero.in/medicellar/registration/updateUser.php";
            }
            
            URL url = new URL(url_string);
            
            URLConnection con = url.openConnection();
            // activate the output
            con.setDoOutput(true);
            
            PrintStream ps = new PrintStream(con.getOutputStream());
            // send your parameters to your site
            ps.print("mac="+mac);
            ps.print("&ip="+ip);
            ps.print("&productId="+productId);
            ps.print("&productKey="+productKey);
            ps.print("&userName="+userName);
            ps.print("&userMail="+userMail);
            ps.print("&contact="+contact);
            ps.print("&password="+password);
            
            // we have to get the input stream in order to actually send the request
            // con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = null;
            String res = null;
            while ((line = in.readLine()) != null) {
                System.out.println("Activation details: "+line);
                res+=line;
            }
//            res=res.substring(res.length()-8, res.length());
            System.out.println("recieved res:"+res);
            if(res.contains("success")){
                k=true;
                System.out.println("returning 'k' true");
            }else{
                k=false;
                System.out.println("returning 'k' false");
            }
            // close the print stream
            ps.close();
        } 
        catch (MalformedURLException e) {
            e.printStackTrace();
            connectionMsg();
        } 
        catch (IOException e) {
            e.printStackTrace();
            connectionMsg();
        }
        
        return k;
    }
    
    private void activate(String userName,String userMail,String productId,String productKey,String pass,String ip,String mac){
        
            try {
                
                String date=new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                Statement stmt=con.createStatement();
                Statement stmt1=con.createStatement();
                stmt.executeUpdate("INSERT INTO STOCKUP_ADMIN VALUES('"+productKey+"','"+productId+"','"+userName+"','"+userMail+"','"+pass+"','0','"+ip+"','"+mac+"')");
                
                stmt1.executeUpdate("INSERT INTO ACTIVITY VALUES('ACTIVE','"+date+"','"+date+"')");
                
                jTextField28.setText("");
                jTextField29.setText("");
                jTextField30.setText("");
                jTextField31.setText("");
                jTextField32.setText("");
                jTextField34.setText("");
                jTextField35.setText("");
                jLabel235.setText("");
                jPasswordField1.setText("");
                jPasswordField2.setText("");
                jCheckBox2.setSelected(false);
                active=true;
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }
            
        
        System.out.println("////////////you product is being updated//////////////");
        System.out.println(userName);
        System.out.println(userMail);
        System.out.println(productId);
        System.out.println(productKey);
    }
    private void startRegistration(){
        
        String mac=getSysDetails("mac");
        String ip=getSerialNumber();
        String productKey=jTextField28.getText()+jTextField29.getText()+jTextField30.getText()+jTextField31.getText();
        String userMail=jTextField34.getText();
        String userName=jTextField32.getText();
        String password=new String(jPasswordField2.getPassword());
        String contact=jTextField35.getText();
        String productId=userMail.substring(0,userMail.indexOf("@"))+jTextField31.getText();
            
        if(keyAvailable(productKey)){
            //user details will be added to table
            if(activateAccount(1, mac, ip, productKey, userMail, userName, password, contact, productId)){
                //activate the product for use
                activate(userName,userMail,productId,productKey,password,ip,mac);
                jTabbedPane4.setSelectedIndex(0);
                check_active();
                JOptionPane.showMessageDialog(this, "The product is now ready to use.\nPlease restart the application for better experience.");
            }
        }
        else if(keyRegistered(productKey)){
            if(matchMacIp(mac,ip,productKey)){
                //user details will be updated
                if(activateAccount(0, mac, ip, productKey, userMail, userName, password, contact, productId)){
                    //activate the product for use
                    activate(userName,userMail,productId,productKey,password,ip,mac);
                    jTabbedPane4.setSelectedIndex(0);
                    check_active();
                    JOptionPane.showMessageDialog(this, "The product is now ready to use.\nPlease restart the application for better experience.");
                }
            }
            else {
                //give message that the product is already registered
                jLabel243.setText("Register");
                jTabbedPane5.setSelectedIndex(1);
                JOptionPane.showMessageDialog(this, "The product key you are using is already registered with other user.\nPlease purchase a new key and try.");
            }
        }
        else{
            jLabel243.setText("Register");
            jTabbedPane5.setSelectedIndex(1);
            JOptionPane.showMessageDialog(this, "The product key you are using is not correct. please use correct product key.");            
        }
    }
    private void jLabel243MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel243MouseClicked
        // TODO add your handling code here:
        
        if(jLabel235.getText().equals("Correct")){
            
            if(!jTextField28.getText().equals("") && !jTextField29.getText().equals("") && !jTextField30.getText().equals("") && !jTextField31.getText().equals("") && !jTextField32.getText().equals("") && !jTextField34.getText().equals("") && !jTextField35.getText().equals("") && !jPasswordField1.getPassword().equals("") && !jPasswordField2.getPassword().equals("") && jCheckBox2.isSelected()){
                if((jTextField35.getText().length()==10)){
                    if(String.valueOf(jPasswordField1.getPassword()).equals(String.valueOf(jPasswordField2.getPassword()))){
                        jLabel243.setText("Please Wait...");
                        jTabbedPane5.setSelectedIndex(2);
                        new startActivation().start();
                    }
                    else{
                        JOptionPane.showMessageDialog(this, "Please make sure that the Re-password matches the password field.");
                    }
                }
                else{
                    JOptionPane.showMessageDialog(this, "Please give a valid contact no. don't add (+91).");
                } 
            }
            else if(!jCheckBox2.isSelected()){
                JOptionPane.showMessageDialog(this, "Please read terms and conditions and check the box.");
            }
            else{
                JOptionPane.showMessageDialog(this, "Please fillup all the fields!!!");
            }
        }
        else if(jLabel235.getText().equals("Already Registered")){
            jLabel243.setText("Please Wait...");
            jTabbedPane5.setSelectedIndex(2);
            new startActivation().start();
        }
        else if(jLabel235.getText().equals("InCorrect")){
                   
            JOptionPane.showMessageDialog(this, "Sorry!!! We can't process your request as the product key is incorrect.\nPlease buy a new key from our website or outlet store.");
        }
        else {
            JOptionPane.showMessageDialog(this, "Please enter the product key properly!!!");
        }
    }//GEN-LAST:event_jLabel243MouseClicked

    private void jTextField28KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField28KeyReleased
        // TODO add your handling code here:
        String str=jTextField28.getText();
        if(str.length()>=4){
            jTextField28.setText(str.toUpperCase());
            jTextField29.requestFocus();
        }
    }//GEN-LAST:event_jTextField28KeyReleased

    private void jTextField29KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField29KeyReleased
        // TODO add your handling code here:
        String str=jTextField29.getText();
        if(str.length()>=4){
            jTextField29.setText(str.toUpperCase());
            jTextField30.requestFocus();
        }
    }//GEN-LAST:event_jTextField29KeyReleased

    private void jTextField30KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField30KeyReleased
        // TODO add your handling code here:
        String str=jTextField30.getText();
        if(str.length()>=4){
            jTextField30.setText(str.toUpperCase());
            jTextField31.requestFocus();
        }
    }//GEN-LAST:event_jTextField30KeyReleased

    private void jTextField31KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField31KeyReleased
        // TODO add your handling code here:
         String str=jTextField31.getText();
        if(str.length()>=4){
            jTextField31.setText(str.toUpperCase());
            jTextField32.requestFocus();
        }
    }//GEN-LAST:event_jTextField31KeyReleased

    private void jTextField30FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField30FocusGained
        // TODO add your handling code here:
        jTextField30.setText("");
    }//GEN-LAST:event_jTextField30FocusGained

    private void jTextField31FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField31FocusGained
        // TODO add your handling code here:
        
        jTextField31.setText("");
        jPanel1.setVisible(true);
        jLabel235.setText("");
    }//GEN-LAST:event_jTextField31FocusGained

    private void jTextField29FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField29FocusGained
        // TODO add your handling code here:
        jTextField29.setText("");
    }//GEN-LAST:event_jTextField29FocusGained

    private void jTextField32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField32ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField32ActionPerformed

    private void jTextField34FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField34FocusLost
        // TODO add your handling code here:
        String email=jTextField34.getText();
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches()){
            jTextField34.requestFocus(true);
        }
    }//GEN-LAST:event_jTextField34FocusLost

    private void jTextField28FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField28FocusGained
        // TODO add your handling code here:  
        jTextField28.setText("");
    }//GEN-LAST:event_jTextField28FocusGained

    private void jTextField31FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField31FocusLost
        jPanel1.setVisible(true);
        new checkKey().start();      
    }//GEN-LAST:event_jTextField31FocusLost

    private void jPanel50MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel50MouseClicked
        // TODO add your handling code here:
        jTabbedPane6.setSelectedIndex(1);
        new loadAd().start();
        
        jTabbedPane2.setSelectedIndex(4);
        jTabbedPane4.setSelectedIndex(1);
        jTabbedPane5.setSelectedIndex(0);
    }//GEN-LAST:event_jPanel50MouseClicked

    private void jLabel243MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel243MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel243MouseEntered

    private void jLabel97MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel97MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(0);
        refresh_jTable2();
        refresh_jTable3();
    
    }//GEN-LAST:event_jLabel97MouseClicked

    private void jLabel99MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel99MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(2);
    }//GEN-LAST:event_jLabel99MouseClicked

    private void jLabel100MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel100MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(1);
    }//GEN-LAST:event_jLabel100MouseClicked

    private void jLabel121MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel121MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(5);
    }//GEN-LAST:event_jLabel121MouseClicked

    private void jLabel125MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel125MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(4);
    }//GEN-LAST:event_jLabel125MouseClicked

    private void jLabel129MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel129MouseClicked
        // TODO add your handling code here:
        refresh_jTable13();
        refresh_jTable17();
        jTabbedPane3.setSelectedIndex(0);
    }//GEN-LAST:event_jLabel129MouseClicked

    private void jLabel134MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel134MouseClicked
        // TODO add your handling code here:
        jTabbedPane3.setSelectedIndex(1);
    }//GEN-LAST:event_jLabel134MouseClicked

    private void jLabel128MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel128MouseClicked
        // TODO add your handling code here:
        refresh_jTable16();
        jTabbedPane3.setSelectedIndex(2);
    }//GEN-LAST:event_jLabel128MouseClicked

    private void jLabel130MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel130MouseClicked
        // TODO add your handling code here:
        jTabbedPane3.setSelectedIndex(3);
    }//GEN-LAST:event_jLabel130MouseClicked

    private void jPanel25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel25MouseClicked
        // TODO add your handling code here:
        if(activity){
            jTabbedPane2.setSelectedIndex(4);
            jTabbedPane4.setSelectedIndex(0);
        }else{
            jTabbedPane2.setSelectedIndex(4);
            jTabbedPane4.setSelectedIndex(1);
            jTabbedPane5.setSelectedIndex(0);
        }
    }//GEN-LAST:event_jPanel25MouseClicked

    private void jComboBox6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox6ItemStateChanged
        // TODO add your handling code here:
        Connection con;
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";
            con=(Connection)DriverManager.getConnection(connectionURL);
            
            Statement stmt=con.createStatement();
            stmt.executeUpdate("UPDATE STOCKUP_ADMIN SET START_PAGE='"+jComboBox6.getSelectedIndex()+"'");
            
            jLabel70.setText("Changed to '"+jComboBox6.getSelectedItem()+"'.");
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
        }
    }//GEN-LAST:event_jComboBox6ItemStateChanged

    private void jTextField25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField25ActionPerformed

    private void jLabel184MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel184MouseClicked
        // TODO add your handling code here:
        refresh_jTable2();
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jLabel184MouseClicked

    private void jLabel183MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel183MouseClicked
        // TODO add your handling code here:
        refresh_jTable3();
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jLabel183MouseClicked

    private void jLabel185MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel185MouseClicked
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jLabel185MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        shortlist(jTextField1.getText());
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jLabel183MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel183MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel183MouseEntered

    private void jLayeredPane2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLayeredPane2MouseClicked
        // TODO add your handling code here:
        if(active){
            jTabbedPane3.setSelectedIndex(2);
            jTabbedPane2.setSelectedIndex(3);
        }else{
            registerMsg();
        }
    }//GEN-LAST:event_jLayeredPane2MouseClicked

    private void jLabel39MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel39MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(5);
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jLabel39MouseClicked

    private void jLabel58MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel58MouseClicked
                // TODO add your handling code here:
        String input_compo1=jTextField11.getText();
        String input_compo2=jTextField12.getText();
        String input_compo3=jTextField13.getText();
        String input_block=jTextField9.getText();
        String input_des=jEditorPane1.getText();
        String input_note=jEditorPane2.getText();
        if (jCheckBox1.isSelected()==false){
            jTextField20.setText("1");
            jTextField10.setText("0");
        }
        //fillup the fields not filled by user
        if(input_compo1.equals("")){
            input_compo1="null";
        }
        if(input_compo2.equals("")){
            input_compo2="null";
        }
        if(input_compo3.equals("")){
            input_compo3="null";
        }
        if(input_block.equals("")){
            input_block="NA";
        }
        if(input_des.equals("")){
            input_des="null";
        }
        if(input_note.equals("")){
            input_note="null";
        }

        try{
            String input_name=jTextField14.getText();
            int input_quantity=Integer.parseInt(jTextField20.getText());
            double input_mrp=Double.parseDouble(jTextField10.getText());

            //checks the value given is valid or not
            if (input_quantity>0 && input_mrp>=0 && !input_name.equals("")){
                Connection con; ResultSet rs;
                try {
                    String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                    con=(Connection)DriverManager.getConnection(connectionURL);
                    Statement stmt=con.createStatement();

                    String query="select * from STOCK";
                    rs = stmt.executeQuery(query);

                    String updated_name=null;
                    boolean med_present=false;
                    int t1=0,t=0;       //t1=previous quantity available in stock... t=total quantity after adding current input quantity given by user

                    while(rs.next()){
                        String name=rs.getString("MED_NAME");
                        if(input_name.equals(name)){
                            updated_name=name;
                            t1=rs.getInt("QUANTITY");   //available quantity fetched from database
                            t=t1+input_quantity;        //total quantity after adding

                            med_present=true;           //set that medicine is already present in stock
                        }
                    }

                    if(med_present){
                        String options[]={"YES, CONTINUE","NO"};
                        if(JOptionPane.showOptionDialog(null, "The medicine '"+updated_name+"' is already in the stock.\nThe details will be updated/overwritten. Do you really want to proceed?","Warning",
                            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0])==JOptionPane.YES_OPTION){
                        if (jCheckBox1.isSelected()){
                            query="UPDATE STOCK SET QUANTITY="+t+",PRICE="+input_mrp+",LOCATION='"+input_block+"' WHERE CAST(MED_NAME AS VARCHAR(128))='"+updated_name+"'";
                            stmt.executeUpdate(query);
                        }
                        if (!input_compo1.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET CONFIG1='"+input_compo1+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_compo2.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET CONFIG2='"+input_compo2+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_compo3.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET CONFIG3='"+input_compo3+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_des.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET DESCRIPTION='"+input_des+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_note.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET NOTE='"+input_note+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        reset_jPanel2();
                    }
                }

                else{
                    rs = stmt.executeQuery("select NAME from MED_DETAILS");

                    while(rs.next()){
                        String name=rs.getString("NAME");
                        if(input_name.equals(name)){
                            updated_name=name;
                            med_present=true;
                        }
                    }

                    if(med_present){
                        String options[]={"YES, CONTINUE","NO"};
                        if(JOptionPane.showOptionDialog(null, "The medicine '"+updated_name+"' is not in the stock but the other details are saved. Do you really want to update the details?","Warning",
                            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0])==JOptionPane.YES_OPTION){

                        if (jCheckBox1.isSelected()){
                            stmt.executeUpdate("INSERT INTO STOCK VALUES ('"+input_name+"',"+input_quantity+","+input_mrp+",'"+input_block+"')");
                        }

                        if (!input_compo1.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET CONFIG1='"+input_compo1+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_compo2.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET CONFIG2='"+input_compo2+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_compo3.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET CONFIG3='"+input_compo3+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_des.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET DESCRIPTION='"+input_des+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }
                        if (!input_note.equals("null")){
                            stmt.executeUpdate("UPDATE MED_DETAILS SET NOTE='"+input_note+"' WHERE CAST(NAME AS VARCHAR(128))='"+updated_name+"'");
                        }

                        reset_jPanel2();
                    }

                }
                else{
                    if (jCheckBox1.isSelected()){
                        stmt.executeUpdate("INSERT INTO STOCK VALUES ('"+input_name+"',"+input_quantity+","+input_mrp+",'"+input_block+"')");
                    }

                    query="INSERT INTO MED_DETAILS VALUES ('"+input_name+"','"+input_compo1+"','"+input_compo2+"','"+input_compo3+"','"+input_des+"','"+input_note+"',0)";
                    stmt.executeUpdate(query);
                    reset_jPanel2();
                }
            }

            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException err){
            System.out.println("Error::"+err.getMessage());
            err.printStackTrace();
        }

        }
        else{
            JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
        }
        }
        catch(Exception err){
            JOptionPane.showMessageDialog(this, "Atleast fill up the name, quantity and price fields!!!");
            System.out.println("Error:--"+err.getMessage());
            err.getStackTrace();
        }
    }//GEN-LAST:event_jLabel58MouseClicked

    private void jLabel237MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel237MouseClicked
        // TODO add your handling code here:
        jTabbedPane6.setSelectedIndex(1);
        new loadAd().start();
        
        jTabbedPane2.setSelectedIndex(4);
        jTabbedPane4.setSelectedIndex(1);
        jTabbedPane5.setSelectedIndex(0);
    }//GEN-LAST:event_jLabel237MouseClicked

    private void jLabel244MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel244MouseClicked
        // TODO add your handling code here:
        new buyKey().start();
    }//GEN-LAST:event_jLabel244MouseClicked

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:

        if(!jTextField15.getText().equals("")||!jTextField16.getText().equals("")){
            String text_name=jTextField15.getText();
            String text_location="NA";
            if(!jTextField22.getText().equals("")){
                text_location=jTextField22.getText();
            }
            try{
                int text_quantity=Integer.parseInt(jTextField16.getText());
                Double text_mrp=0.0;
                if(!jTextField19.getText().equals("")){
                    text_mrp=Double.parseDouble(jTextField19.getText());
                }

                if (text_quantity>0 && text_mrp>=0){
                    Connection con; ResultSet rs;
                    try {
                        String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                        con=(Connection)DriverManager.getConnection(connectionURL);
                        System.out.println("Connecttion SuccessFul");

                        Statement stmt=con.createStatement();

                        rs = stmt.executeQuery("select * from STOCK");
                        int t=0,t1=0;
                        String updated_name=null;
                        boolean med_present=false;

                        while(rs.next()){
                            String name=rs.getString("MED_NAME");
                            if(text_name.equals(name)){
                                System.out.println("present");
                                updated_name=name;
                                t1=rs.getInt("QUANTITY");
                                t=t1+text_quantity;
                                System.out.println("value calculated: "+t);
                                med_present=true;
                            }
                            else{
                                System.out.println("not present");
                            }
                        }

                        if(med_present){
                            stmt.executeUpdate("UPDATE STOCK SET QUANTITY="+t+" WHERE CAST(MED_NAME AS VARCHAR(128))='"+updated_name+"'");
                            if(!jTextField19.getText().equals("")){
                                stmt.executeUpdate("UPDATE STOCK SET PRICE="+text_mrp+" WHERE CAST(MED_NAME AS VARCHAR(128))='"+updated_name+"'");
                            }
                            if(!jTextField22.getText().equals("")){
                                stmt.executeUpdate("UPDATE STOCK SET LOCATION='"+text_location+"' WHERE CAST(MED_NAME AS VARCHAR(128))='"+updated_name+"'");
                            }
                            final_setup(text_name,t1,text_quantity,t);
                        }
                        else{
                            t=t1+text_quantity;
                            if (!jTextField19.getText().equals("")){

                                stmt.executeUpdate("INSERT INTO STOCK VALUES ('"+text_name+"',"+text_quantity+","+text_mrp+",'"+text_location+"')");

                                rs = stmt.executeQuery("select name from MED_DETAILS");

                                boolean update_main=false;

                                while(rs.next()){
                                    if(rs.getString("name").equals(text_name)){
                                        update_main=true;
                                    }
                                }
                                if (!update_main){
                                    String query="INSERT INTO MED_DETAILS VALUES ('"+text_name+"','"+null+"','"+null+"','"+null+"','"+null+"','"+null+"',0)";
                                    stmt.executeUpdate(query);
                                    if(show_newAddition_warning==0){
                                        Object[] options = { "OK", "DON'T REMIND ME AGAIN"};
                                        String message =    "Let us inform you that, "+text_name+" is being included for the first time in your shop."
                                        + "\nso please, update it's details for convenience in your future refereneces.";

                                        if(JOptionPane.showOptionDialog(null, message,"Warning",
                                            JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE,
                                            null, options, options[0])==JOptionPane.NO_OPTION){
                                        show_newAddition_warning=1;
                                    }
                                }
                            }
                            final_setup(text_name,t1,text_quantity,t);
                        }
                        else{
                            JOptionPane.showMessageDialog(this, "There is no details about the price of '"+text_name+"'. please, fillup the price details.");
                        }
                    }

                    rs.close();
                    stmt.close();
                    con.close();
                }
                catch(SQLException err){
                    System.out.println("Error::"+err.getMessage());
                    err.printStackTrace();
                }
            }
            else{
                JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            }
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(this, "Please give a valid input!!!");
            System.out.println("::"+e.getMessage());
            e.getStackTrace();
        }

        }
        else{
            JOptionPane.showMessageDialog(this, "Please fill up the fields with valid input!!!");
        }
        jTextField15.requestFocus(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jTextField19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField19ActionPerformed
        // TODO add your handling code here:
        jTextField22.requestFocus(true);
    }//GEN-LAST:event_jTextField19ActionPerformed

    private void jTextField16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField16ActionPerformed
        // TODO add your handling code here:
        jTextField19.requestFocus(true);
    }//GEN-LAST:event_jTextField16ActionPerformed

    private void jTextField22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField22ActionPerformed
        // TODO add your handling code here:
        jButton8ActionPerformed(evt);
    }//GEN-LAST:event_jTextField22ActionPerformed

    private void jTextField15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField15ActionPerformed
        // TODO add your handling code here:
        jTextField16.requestFocus(true);
    }//GEN-LAST:event_jTextField15ActionPerformed

    private void jLabel66MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel66MouseClicked
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(3);
    }//GEN-LAST:event_jLabel66MouseClicked
int k=0;    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    new UIManager();
                    UIManager.setLookAndFeel(info.getClassName());
                    //javax.swing.UIManager.put(info, info);
                    /*UIManager.put("OptionPane.background", new ColorUIResource(0, 255, 0));
                    UIManager.put("OptionPane.messagebackground", Color.BLUE);
                    UIManager.put("Panel.background", Color.GREEN);*/
                    
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
             home frame=new home();
                frame.setIconImage(new ImageIcon(getClass().getResource("mc_logo.png")).getImage());
                frame.setVisible(true);
            }
        });
        
    }
class startActivation extends Thread {
    public void run(){
        startRegistration();
        jTabbedPane5.setSelectedIndex(1);
    }
}

class registerPage extends Thread {
    public void run(){
        if(internetReachable()){
            jTabbedPane5.setSelectedIndex(1);
        }
        else{
            connectionMsg();
            jTabbedPane5.setSelectedIndex(0);
            jTabbedPane6.setSelectedIndex(2);
        }
    }
}

class loadAd extends Thread {
    public void run(){
        if(internetReachable()){
            try{
                //loads the image of 900*300 px
                jLabel67.setIcon(new ImageIcon(ImageIO.read(new URL("http://sybero.in/medicellar/load_aid.png"))));
            }catch(Exception e){
                
            }
            jTabbedPane6.setSelectedIndex(0);
        }
        else{
            jTabbedPane6.setSelectedIndex(2);
        }
    }
}
        
class buyKey extends Thread {
    public void run(){
        if(internetReachable()){
            try{     
                Desktop.getDesktop().browse(new URI("http://sybero.in/medicellar/index.php?buy_newKey=true"));   
            }catch(Exception e){
                e.getStackTrace();
                connectionMsg();
            }
            jTabbedPane6.setSelectedIndex(2);
        }
        else{
            connectionMsg();
            jTabbedPane5.setSelectedIndex(0);
            jTabbedPane6.setSelectedIndex(2);
        }
    }
}

class changePass extends Thread {
    public void run(){
        if(internetReachable()){
            changePassword();
        }
        else{
            connectionMsg();
        }
    }
}

class checkKey extends Thread {
    public void run(){
        String productKey=jTextField28.getText()+jTextField29.getText()+jTextField30.getText()+jTextField31.getText();
        if(keyAvailable(productKey)){
            jLabel235.setText("Correct");
            jLabel235.setForeground(Color.green);
        }
        else if(keyRegistered(productKey)){
            jLabel235.setText("Already Registered");
            jLabel235.setForeground(Color.blue);
        }
        else if(!keyRegistered(productKey)){
            jLabel235.setText("Incorrect");
            jLabel235.setForeground(Color.red);
        }
        else{
            jLabel235.setText("Error Occured");
            jLabel235.setForeground(Color.red);            
        }
        jPanel1.setVisible(false);
    }
}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JComboBox jComboBox9;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JEditorPane jEditorPane2;
    private javax.swing.JEditorPane jEditorPane3;
    private javax.swing.JEditorPane jEditorPane4;
    private javax.swing.JEditorPane jEditorPane5;
    private javax.swing.JEditorPane jEditorPane6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel111;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel116;
    private javax.swing.JLabel jLabel117;
    private javax.swing.JLabel jLabel118;
    private javax.swing.JLabel jLabel119;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel120;
    private javax.swing.JLabel jLabel121;
    private javax.swing.JLabel jLabel122;
    private javax.swing.JLabel jLabel123;
    private javax.swing.JLabel jLabel124;
    private javax.swing.JLabel jLabel125;
    private javax.swing.JLabel jLabel126;
    private javax.swing.JLabel jLabel128;
    private javax.swing.JLabel jLabel129;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel130;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel132;
    private javax.swing.JLabel jLabel133;
    private javax.swing.JLabel jLabel134;
    private javax.swing.JLabel jLabel135;
    private javax.swing.JLabel jLabel136;
    private javax.swing.JLabel jLabel137;
    private javax.swing.JLabel jLabel138;
    private javax.swing.JLabel jLabel139;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel140;
    private javax.swing.JLabel jLabel141;
    private javax.swing.JLabel jLabel142;
    private javax.swing.JLabel jLabel143;
    private javax.swing.JLabel jLabel144;
    private javax.swing.JLabel jLabel145;
    private javax.swing.JLabel jLabel146;
    private javax.swing.JLabel jLabel147;
    private javax.swing.JLabel jLabel148;
    private javax.swing.JLabel jLabel149;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel150;
    private javax.swing.JLabel jLabel151;
    private javax.swing.JLabel jLabel152;
    private javax.swing.JLabel jLabel153;
    private javax.swing.JLabel jLabel154;
    private javax.swing.JLabel jLabel155;
    private javax.swing.JLabel jLabel156;
    private javax.swing.JLabel jLabel157;
    private javax.swing.JLabel jLabel158;
    private javax.swing.JLabel jLabel159;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel164;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel172;
    private javax.swing.JLabel jLabel173;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel180;
    private javax.swing.JLabel jLabel181;
    private javax.swing.JLabel jLabel182;
    private javax.swing.JLabel jLabel183;
    private javax.swing.JLabel jLabel184;
    private javax.swing.JLabel jLabel185;
    private javax.swing.JLabel jLabel186;
    private javax.swing.JLabel jLabel187;
    private javax.swing.JLabel jLabel188;
    private javax.swing.JLabel jLabel189;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel190;
    private javax.swing.JLabel jLabel191;
    private javax.swing.JLabel jLabel192;
    private javax.swing.JLabel jLabel193;
    private javax.swing.JLabel jLabel194;
    private javax.swing.JLabel jLabel195;
    private javax.swing.JLabel jLabel196;
    private javax.swing.JLabel jLabel197;
    private javax.swing.JLabel jLabel198;
    private javax.swing.JLabel jLabel199;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel200;
    private javax.swing.JLabel jLabel201;
    private javax.swing.JLabel jLabel202;
    private javax.swing.JLabel jLabel203;
    private javax.swing.JLabel jLabel204;
    private javax.swing.JLabel jLabel205;
    private javax.swing.JLabel jLabel206;
    private javax.swing.JLabel jLabel207;
    private javax.swing.JLabel jLabel208;
    private javax.swing.JLabel jLabel209;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel210;
    private javax.swing.JLabel jLabel211;
    private javax.swing.JLabel jLabel212;
    private javax.swing.JLabel jLabel213;
    private javax.swing.JLabel jLabel214;
    private javax.swing.JLabel jLabel215;
    private javax.swing.JLabel jLabel216;
    private javax.swing.JLabel jLabel217;
    private javax.swing.JLabel jLabel218;
    private javax.swing.JLabel jLabel219;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel220;
    private javax.swing.JLabel jLabel221;
    private javax.swing.JLabel jLabel222;
    private javax.swing.JLabel jLabel223;
    private javax.swing.JLabel jLabel224;
    private javax.swing.JLabel jLabel225;
    private javax.swing.JLabel jLabel226;
    private javax.swing.JLabel jLabel227;
    private javax.swing.JLabel jLabel228;
    private javax.swing.JLabel jLabel229;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel230;
    private javax.swing.JLabel jLabel231;
    private javax.swing.JLabel jLabel232;
    private javax.swing.JLabel jLabel233;
    private javax.swing.JLabel jLabel234;
    private javax.swing.JLabel jLabel235;
    private javax.swing.JLabel jLabel236;
    private javax.swing.JLabel jLabel237;
    private javax.swing.JLabel jLabel238;
    private javax.swing.JLabel jLabel239;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel240;
    private javax.swing.JLabel jLabel241;
    private javax.swing.JLabel jLabel242;
    private javax.swing.JLabel jLabel243;
    private javax.swing.JLabel jLabel244;
    private javax.swing.JLabel jLabel245;
    private javax.swing.JLabel jLabel246;
    private javax.swing.JLabel jLabel247;
    private javax.swing.JLabel jLabel248;
    private javax.swing.JLabel jLabel249;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel250;
    private javax.swing.JLabel jLabel251;
    private javax.swing.JLabel jLabel252;
    private javax.swing.JLabel jLabel253;
    private javax.swing.JLabel jLabel254;
    private javax.swing.JLabel jLabel255;
    private javax.swing.JLabel jLabel256;
    private javax.swing.JLabel jLabel257;
    private javax.swing.JLabel jLabel258;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JPasswordField jPasswordField3;
    private javax.swing.JPasswordField jPasswordField4;
    private javax.swing.JPasswordField jPasswordField5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane24;
    private javax.swing.JScrollPane jScrollPane26;
    private javax.swing.JScrollPane jScrollPane27;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTabbedPane jTabbedPane6;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable10;
    private javax.swing.JTable jTable11;
    private javax.swing.JTable jTable12;
    private javax.swing.JTable jTable13;
    private javax.swing.JTable jTable15;
    private javax.swing.JTable jTable16;
    private javax.swing.JTable jTable17;
    private javax.swing.JTable jTable18;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JTable jTable7;
    private javax.swing.JTable jTable8;
    private javax.swing.JTable jTable9;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField22;
    private javax.swing.JTextField jTextField23;
    private javax.swing.JTextField jTextField24;
    private javax.swing.JTextField jTextField25;
    private javax.swing.JTextField jTextField28;
    private javax.swing.JTextField jTextField29;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField30;
    private javax.swing.JTextField jTextField31;
    private javax.swing.JTextField jTextField32;
    private javax.swing.JTextField jTextField34;
    private javax.swing.JTextField jTextField35;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private org.jdesktop.swingx.JXDatePicker jXDatePicker1;
    private org.jdesktop.swingx.JXDatePicker jXDatePicker2;
    // End of variables declaration//GEN-END:variables
}

