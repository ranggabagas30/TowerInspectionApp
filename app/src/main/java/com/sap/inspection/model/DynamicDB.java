package com.sap.inspection.model;

public class DynamicDB {
	
//	public void createDynamicDatabase(Context context,String tableName,ArrayList<String> title) {
//
//        Log.i("INSIDE createLoginDatabase() Method","*************creatLoginDatabase*********");
//        try {
//
//            int i;
//            String querryString;
////            myDataBase = context.openOrCreateDatabase("Db",Context.MODE_WORLD_WRITEABLE, null);         //Opens database in writable mode.
//            myDataBase = DbRepository.getInstance().getDB();
//            //System.out.println("Table Name : "+tableName.get(0));
//
//            querryString = title.get(0)+" VARCHAR(30),";
//            Log.d("**createDynamicDatabase", "in oncreate");
//            for(i=1;i<title.size()-1;i++)
//            {               
//                querryString += title.get(i);
//                querryString +=" VARCHAR(30)";
//                querryString +=",";
//            }
//            querryString+= title.get(i) +" VARCHAR(30)";
//
//            querryString = "CREATE TABLE IF NOT EXISTS " + tableName + "("+querryString+");";
//
//            System.out.println("Create Table Stmt : "+ querryString);
//
//            myDataBase.execSQL(querryString);
//
//        } catch (SQLException ex) {
//            Log.i("CreateDB Exception ",ex.getMessage());
//        }
//    }
//    public void insert(Context context,ArrayList<String> array_vals,ArrayList<String> title,String TABLE_NAME) {
//        Log.d("Inside Insert","Insertion starts for table name: "+TABLE_NAME);
//        myDataBase = context.openOrCreateDatabase("Db",Context.MODE_WORLD_WRITEABLE, null);         //Opens database in writable mode.
//        String titleString=null;
//        String markString= null;
//        int i;
//        titleString = title.get(0)+",";
//        markString = "?,";
//        Log.d("**createDynamicDatabase", "in oncreate");
//        for(i=1;i<title.size()-1;i++)
//        {               
//            titleString += title.get(i);
//            titleString +=",";
//            markString += "?,";
//        }
//        titleString+= title.get(i);
//        markString += "?";
//
//        //System.out.println("Title String: "+titleString);
//        //System.out.println("Mark String: "+markString);
//
//
//        String INSERT="insert into "+ TABLE_NAME + "("+titleString+")"+ "values" +"("+markString+")";
//        System.out.println("Insert statement: "+INSERT);
//        //System.out.println("Array size iiiiii::: "+array_vals.size());
//        //this.insertStmt = this.myDataBase.compileStatement(INSERT);
//        int s=0;
//
//        while(s<array_vals.size()){
//
//        System.out.println("Size of array1"+array_vals.size());
//                //System.out.println("Size of array"+title.size());
//        int j=1;
//        this.insertStmt = this.myDataBase.compileStatement(INSERT);
//        for(int k =0;k< title.size();k++)
//        {
//
//            //System.out.println("Value of column "+title+" is "+array_vals.get(k+s));
//            //System.out.println("PRINT S:"+array_vals.get(k+s));
//            System.out.println("BindString: insertStmt.bindString("+j+","+ array_vals.get(k+s)+")");
//            insertStmt.bindString(j, array_vals.get(k+s));
//
//
//
//            j++;
//        }
//
//        s+=title.size();
//
//        }
//        insertStmt.executeInsert();
//    }


}
