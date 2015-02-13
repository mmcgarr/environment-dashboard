package org.jenkinsci.plugins.environmentdashboard;

import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.ModelObjectWithContextMenu;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.kohsuke.stapler.QueryParameter;
import org.jenkinsci.plugins.environmentdashboard.utils.DBConnection;

import java.util.List;
import net.sf.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import hudson.model.Describable;
import java.util.logging.Logger;

/**
 * Entry point to all the UI samples.
 * 
 * @author Kohsuke Kawaguchi
 */
@Extension
public class Root implements RootAction, ModelObjectWithContextMenu{

    private Connection conn = null;
    private Statement stat = null;
    private static final Logger LOGGER = Logger.getLogger(Root.class.getName());

    public String getIconFileName() {
        return "gear.png";
    }

    public String getDisplayName() {
        return "Environment Dashboard";
    }

    public String getUrlName() {
        return "env-dash";
    }

    public ArrayList<String> getCustomDBColumns(){
        ArrayList<String> columns;
        columns = new ArrayList<String>();
        String queryString="SELECT DISTINCT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS where TABLE_NAME='ENV_DASHBOARD';";
        String[] fields = {"envComp", "compName", "envName", "buildstatus", "buildJobUrl", "jobUrl", "buildNum", "created_at", "packageName"};
        ResultSet rs;
        boolean columnFound = false;
            try {
                //Get DB connection
                conn = DBConnection.getConnection();
                
                assert conn != null;
                stat = conn.createStatement();
                assert stat != null;
                rs = stat.executeQuery(queryString);

                String col = "";
                while (rs.next()) {
                    columnFound=false;
                    col = rs.getString("COLUMN_NAME");
                    for (String presetColumn : fields){
                        if (col.toLowerCase().equals(presetColumn.toLowerCase())){
                            columnFound = true;
                            break;
                        }
                    }
                    if (!columnFound){
                        columns.add(col.toLowerCase());
                    }
                }
                DBConnection.closeConnection();
            } catch (SQLException e) {
                System.out.println("E11" + e.getMessage());
                return new ArrayList<String>();
            }
            System.out.println("Getting DB columns for db management screen");
        return columns;
    }

    @SuppressWarnings("unused")
    public void doDropColumn(@QueryParameter("selectedIds") final String Columns, StaplerRequest request,
                                    StaplerResponse response) throws IOException{
  //      if ("".equals(Columns)){
  //          return; 
  //      }
        LOGGER.log(java.util.logging.Level.SEVERE, "Running correct doDropColumn func");
        System.out.println("In doDropColumn function");
        String queryString = "ALTER TABLE ENV_DASHBOARD DROP COLUMN " + Columns + ";";
        //Get DB connection
        conn = DBConnection.getConnection();

        try {
            assert conn != null;
            stat = conn.createStatement();
        } catch (SQLException e) {
            return;
        }
        try {
            assert stat != null;
            stat.execute(queryString);
        } catch (SQLException e) {
            DBConnection.closeConnection();
            return; 
        } 
        DBConnection.closeConnection();

        return;
    }

    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        return new ContextMenu().from(this,request,response); 
    }

}
