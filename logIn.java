/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package medistore;
import java.util.Date;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static medistore.home.generatePassword;
import static medistore.home.internetReachable;
import org.apache.derby.jdbc.EmbeddedDriver;
import javax.swing.plaf.ColorUIResource;
/**
 *
 * @author Rohit
 */
    
public class logIn extends javax.swing.JFrame {


    @Override
    public void setIconImage(Image image) {
        super.setIconImage(image); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Creates new form logIn
     */
    boolean active=false;
    
    public logIn() {
        initComponents();
        check_active();
        create_dataTables();
    }
    
    private void create_dataTables(){        
        if(!active){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();
                DatabaseMetaData dbm = con.getMetaData();

                ResultSet tables = dbm.getTables(null, null, "%", null);
                boolean activity=false;
                boolean lastday_sale=false;
                boolean today_sale=false;
                boolean med_details=false;
                boolean stockup_admin=false;
                boolean stock=false;
                boolean shortlist=false;
                boolean note=false;
                boolean trial_details=false;
                boolean out_of_stock=false;
                Date myDate=new Date();
                String install_date=new SimpleDateFormat("dd/MM/yyyy").format(myDate);
                
                while(tables.next()) {
                    String name=tables.getString(3);
                    if(name.equals("ACTIVITY")){
                        activity=true;
                    }else if(name.equals("LASTDAY_SALE")){
                        lastday_sale=true;
                    }else if(name.equals("TODAY_SALE")){
                        today_sale=true;
                    }else if(name.equals("MED_DETAILS")){
                        med_details=true;
                    }else if(name.equals("STOCKUP_ADMIN")){
                        stockup_admin=true;
                    }else if(name.equals("STOCK")){
                        stock=true;
                    }else if(name.equals("SHORTLIST")){
                        shortlist=true;
                    }else if(name.equals("NOTE")){
                        note=true;
                    }else if(name.equals("OUT_OF_STOCK")){
                        out_of_stock=true;
                    }else if(name.equals("TRIAL_DETAILS")){
                        trial_details=true;
                    }
                    
                System.out.println("under while: "+name);
                }

                if(!activity){
                    stmt.executeUpdate("CREATE TABLE ACTIVITY (ACTIVE varchar(255),REG_DATE varchar(255),LAST_LOGIN varchar(255))");
                    stmt.executeUpdate("INSERT INTO ACTIVITY VALUES('INACTIVE','NA','"+install_date+"')");
                }
                if(!lastday_sale){
                    stmt.executeUpdate("CREATE TABLE LASTDAY_SALE (NAME varchar(255),QUANTITY INTEGER,PRICE DOUBLE,TIME varchar(255),DATE varchar(255))");
                }
                if(!today_sale){
                    stmt.executeUpdate("CREATE TABLE TODAY_SALE (NAME varchar(255),QUANTITY INTEGER,PRICE DOUBLE,TIME varchar(255),DATE varchar(255))");
                }
                if(!med_details){
                    stmt.executeUpdate("CREATE TABLE MED_DETAILS (NAME varchar(255),CONFIG1 varchar(255),CONFIG2 varchar(255),CONFIG3 varchar(255),DESCRIPTION varchar(255), NOTE varchar(255),FREQUENCY bigint)");
                }
                if(!stockup_admin){
                    stmt.executeUpdate("CREATE TABLE STOCKUP_ADMIN (PRODUCTKEY varchar(255),PRODUCTID varchar(255),USERNAME varchar(255),USERMAIL varchar(255),PASSWORD varchar(255),START_PAGE varchar(255),COMPUTER_ID varchar(255),MAC_ID varchar(255))");
                }
                if(!stock){
                    stmt.executeUpdate("CREATE TABLE STOCK (MED_NAME varchar(255),QUANTITY DOUBLE,PRICE DOUBLE, LOCATION varchar(255))");
                }
                if(!shortlist){
                    stmt.executeUpdate("CREATE TABLE SHORTLIST (NAME varchar(255),DATE varchar(255),TIME varchar(255),FREQUENCY bigint)");
                }
                if(!out_of_stock){
                    stmt.executeUpdate("CREATE TABLE OUT_OF_STOCK (NAME varchar(255))");
                }
                if(!note){
                    stmt.executeUpdate("CREATE TABLE NOTE (NAME varchar(255),CONTENT varchar(10000),DATE varchar(255))");
                }
                if(!trial_details){
                    stmt.executeUpdate("CREATE TABLE TRIAL_DETAILS (INSTALL_DATE varchar(255), ATTEMPTS varchar(255))");
                    stmt.executeUpdate("INSERT INTO TRIAL_DETAILS VALUES('"+install_date+"','11')");
                }

                ResultSet rs=stmt.executeQuery("SELECT * FROM TRIAL_DETAILS");
                while(rs.next()){
                    if(Integer.valueOf(rs.getString("ATTEMPTS"))>0){
                        jPanel1.setVisible(true);
                        jLabel218.setText("On Trial!!!");
                    }
                    System.out.println("attempts:"+rs.getString("ATTEMPTS"));
                }
                
                
                tables.close();
                stmt.close();
                con.close();
            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
                err.getStackTrace();
                JOptionPane.showMessageDialog(this, "An instance of this application is already running.\nif not, then please reinstall or contact our support team.");
                //JOptionPane.showMessageDialog(this, "Error: "+err.getMessage());
                new disposeMe().start();
            }
        }
    }
    
    
class disposeMe extends Thread {
    public void run(){
       logIn.this.dispose();
    }
}
    private void update_trial(){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                Connection con=(Connection)DriverManager.getConnection(connectionURL);
                Statement stmt=con.createStatement();
                
                Date myDate=new Date();
                String date=new SimpleDateFormat("dd/MM/yyyy").format(myDate);
                
                int attempts=0;
                
                ResultSet rs=stmt.executeQuery("SELECT * FROM TRIAL_DETAILS");
                while(rs.next()){
                    attempts=Integer.valueOf(rs.getString("ATTEMPTS"));
                }
                
                if(attempts>0){
                    rs = stmt.executeQuery("SELECT * FROM ACTIVITY");
                    while(rs.next()) {
                        if(!rs.getString("LAST_LOGIN").equals(date)){
                            attempts=attempts-1;
                        }
                    }
                    
                    stmt.executeUpdate("UPDATE TRIAL_DETAILS SET ATTEMPTS='"+String.valueOf(attempts)+"'");
//                    jPanel1.setVisible(true);
//                    jLabel218.setText("On Trial!!!");
                }
                
                
                rs.close();
                stmt.close();
                con.close();
            }
        catch(SQLException err){
            System.out.println("err::"+err.getMessage());
            err.getStackTrace();
        }
    }
    
    
    private void check_active(){
        
        ResultSet rs;
        try {
            String connectionURL = "jdbc:derby:MediCellarDB;create=true";

            Connection con=(Connection)DriverManager.getConnection(connectionURL);

            Statement stmt=con.createStatement();

            //check wether the product is active or not
            rs=stmt.executeQuery("SELECT * FROM ACTIVITY");
            while(rs.next()){
                if(rs.getString("ACTIVE").equals("ACTIVE")){
                    active=true;
                    jPanel1.setVisible(false);
                    jLabel3.setForeground(Color.white);
                }
                else{
                    jPanel1.setVisible(true);
                }
            }
           
            rs.close();
            stmt.close();
            con.close();
        }
        catch(SQLException e){
            e.getStackTrace();
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

        jPanel2 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jLabel217 = new javax.swing.JLabel();
        jLabel245 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel218 = new javax.swing.JLabel();
        jLabel248 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel246 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel249 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel247 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MediCellar");
        setBackground(new java.awt.Color(255, 255, 255));
        setFocusable(false);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(600, 400));
        setResizable(false);
        setSize(new java.awt.Dimension(600, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMinimumSize(new java.awt.Dimension(600, 400));
        jPanel2.setPreferredSize(new java.awt.Dimension(600, 400));

        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 48)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(55, 77, 87));
        jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/medistore/mc_80.png"))); // NOI18N

        jPasswordField1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jPasswordField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPasswordField1.setName(""); // NOI18N
        jPasswordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField1ActionPerformed(evt);
            }
        });

        jLabel3.setForeground(new java.awt.Color(160, 160, 160));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("please use 'medicellar' as password, you can change it later.");

        jLabel217.setBackground(new java.awt.Color(55, 150, 198));
        jLabel217.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel217.setForeground(new java.awt.Color(255, 255, 255));
        jLabel217.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel217.setText("Log me in");
        jLabel217.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel217.setOpaque(true);
        jLabel217.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel217MouseClicked(evt);
            }
        });

        jLabel245.setFont(new java.awt.Font("Microsoft Yi Baiti", 1, 15)); // NOI18N
        jLabel245.setForeground(new java.awt.Color(55, 150, 198));
        jLabel245.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel245.setText("Forgotten your password?");
        jLabel245.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel245.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel245.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel245MouseClicked(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(250, 250, 250));
        jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel218.setBackground(new java.awt.Color(250, 250, 250));
        jLabel218.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel218.setForeground(new java.awt.Color(55, 150, 198));
        jLabel218.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel218.setText("Unregistered!!!");
        jLabel218.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel218.setOpaque(true);
        jLabel218.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel218MouseClicked(evt);
            }
        });
        jPanel1.add(jLabel218, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        jLabel248.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel248.setForeground(new java.awt.Color(55, 150, 198));
        jLabel248.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel248.setText("©2015 Sybero Infotech");
        jLabel248.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel248.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel248.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel248MouseClicked(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel246.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel246.setForeground(new java.awt.Color(55, 150, 198));
        jLabel246.setText("Privacy Policy");
        jLabel246.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel246.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel246.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel246MouseClicked(evt);
            }
        });
        jPanel3.add(jLabel246);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel1.setText("|");
        jPanel3.add(jLabel1);

        jLabel249.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel249.setForeground(new java.awt.Color(55, 150, 198));
        jLabel249.setText("Terms of Use");
        jLabel249.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel249.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel249.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel249MouseClicked(evt);
            }
        });
        jPanel3.add(jLabel249);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel2.setText("|");
        jPanel3.add(jLabel2);

        jLabel247.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jLabel247.setForeground(new java.awt.Color(55, 150, 198));
        jLabel247.setText("contact us");
        jLabel247.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel247.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel247.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel247MouseClicked(evt);
            }
        });
        jPanel3.add(jLabel247);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(81, 81, 81)
                                .addComponent(jLabel217, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPasswordField1)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel248, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel245, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel217, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel245)
                .addGap(58, 58, 58)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel248)
                .addContainerGap())
        );

        jPasswordField1.setEchoChar('●');

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
        jLabel217MouseClicked(null);
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void jLabel217MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel217MouseClicked
        
        if(!String.valueOf(jPasswordField1.getPassword()).equals("")){
            try {
                String connectionURL = "jdbc:derby:MediCellarDB;create=true";
                
                Connection con=(Connection)DriverManager.getConnection(connectionURL);

                Statement stmt=con.createStatement();

                boolean matched=false;
                boolean pass_found=false;
                Date myDate=new Date();
                String login_date=new SimpleDateFormat("dd/MM/yyyy").format(myDate);
                
                ResultSet rs=stmt.executeQuery("SELECT * FROM STOCKUP_ADMIN");
                while(rs.next()){
                    if(String.valueOf(jPasswordField1.getPassword()).equals(rs.getString("PASSWORD"))){
                        matched=true;
                    }
                    pass_found=true;
                }
                
                
                if(matched){
                    login1();
                    update_trial();
                    stmt.executeUpdate("UPDATE ACTIVITY SET LAST_LOGIN='"+login_date+"'");
                }
                else if(!pass_found && String.valueOf(jPasswordField1.getPassword()).equals("medicellar")){
                    login1();
                    update_trial();
                    stmt.executeUpdate("UPDATE ACTIVITY SET LAST_LOGIN='"+login_date+"'");
                }
                else{
                    jPasswordField1.setText("");
                    JOptionPane.showMessageDialog(this, "Incorrect Password!!!");
                    jPasswordField1.requestFocus();
                }
                
                
                rs.close();
                stmt.close();
                con.close();
                

            }
            catch(SQLException err){
                System.out.println("Error::"+err.getMessage());
            }   
        }else{
            JOptionPane.showMessageDialog(this, "Please enter the password and then try.");
        }
    }//GEN-LAST:event_jLabel217MouseClicked

    private void jLabel218MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel218MouseClicked
        // TODO add your handling code here:
      jPanel1MouseClicked(evt);
    }//GEN-LAST:event_jLabel218MouseClicked

    private void jLabel245MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel245MouseClicked
        // TODO add your handling code here:
        if(active){
            jLabel245.setText("Changing password...");
            jLabel245.setIcon(new ImageIcon(getClass().getResource("ring (2).gif")));
            new changePass().start();
        }else{
            JOptionPane.showMessageDialog(this, "Since the product is not registered yet... you can't change the password.");
        }

    }//GEN-LAST:event_jLabel245MouseClicked

    private void jLabel246MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel246MouseClicked
        // TODO add your handling code here:
        try{     
                Desktop.getDesktop().browse(new URI("file:///C:/Program%20Files%20(x86)/Sybero%20Infotech/MediCellar/MediCellar-PrivacyPolicy.pdf"));   
            }catch(Exception e){
                e.getStackTrace();
                try{
                Desktop.getDesktop().browse(new URI("http://sybero.in/medicellar/docs/MediCellar-PrivacyPolicy.pdf"));   
                }
                catch(Exception err){
                    connectionMsg();
                }
            }
    }//GEN-LAST:event_jLabel246MouseClicked

    private void jLabel247MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel247MouseClicked
        // TODO add your handling code here:
        try{     
                Desktop.getDesktop().browse(new URI("http://sybero.in/medicellar/contacts.php"));   
            }catch(Exception e){
                e.getStackTrace();
                connectionMsg();
            }
    }//GEN-LAST:event_jLabel247MouseClicked

    private void jLabel248MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel248MouseClicked
        // TODO add your handling code here:
        try{     
                Desktop.getDesktop().browse(new URI("http://sybero.in/index.php"));   
            }catch(Exception e){
                e.getStackTrace();
                connectionMsg();
            }
    }//GEN-LAST:event_jLabel248MouseClicked

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
        // TODO add your handling code here:
        if(jLabel218.getText().equals("Unregistered!!!")){
            new login().start();
            jLabel218.setText(null);
            jLabel218.setIcon(new ImageIcon(getClass().getResource("ring (2).gif")));
        }
        
    }//GEN-LAST:event_jPanel1MouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowOpened

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowActivated

    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowIconified

    private void jLabel249MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel249MouseClicked
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
    }//GEN-LAST:event_jLabel249MouseClicked

    private void login1(){
        jLabel217.setBackground(Color.white);
        jLabel217.setForeground(new Color(55,150,198));
        jLabel217.setText("Logging in...");
        jLabel217.setIcon(new ImageIcon(getClass().getResource("ring (2).gif")));
        new login().start();
    }
    
    
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

                jLabel245.setText("Forgotten your password?");
                jLabel245.setIcon(null);
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

        jLabel245.setText("Forgotten your password?");
        jLabel245.setIcon(null);
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
    
private void connectionMsg(){
    JOptionPane.showMessageDialog(this, "Please make sure that you are connected with a working internet connnection.");
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

    
    
class login extends Thread {
    public void run(){
       home frame=new home();
       frame.setIconImage(new ImageIcon(getClass().getResource("mc_logo.png")).getImage());
       frame.setVisible(true);
       logIn.this.dispose();
    }
}
    
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
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(logIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(logIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(logIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(logIn.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                logIn frame=new logIn();
                frame.setIconImage(new ImageIcon(getClass().getResource("mc_logo.png")).getImage());
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel217;
    private javax.swing.JLabel jLabel218;
    private javax.swing.JLabel jLabel245;
    private javax.swing.JLabel jLabel246;
    private javax.swing.JLabel jLabel247;
    private javax.swing.JLabel jLabel248;
    private javax.swing.JLabel jLabel249;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPasswordField jPasswordField1;
    // End of variables declaration//GEN-END:variables
}
