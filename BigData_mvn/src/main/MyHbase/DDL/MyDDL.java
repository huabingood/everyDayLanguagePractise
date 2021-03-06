package DDL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;

/**
 * 实现表的创建，删除等基本功能
 * 基于HBase1.2.0API
 * DDL
 * @author huabingood@qq.com
 */
public class MyDDL {
    /**
     * 记录日志
     * 但是好像每个类都要写这么一句，但是不知道怎么改进
     */
    private static Log myLog = LogFactory.getLog(MyDDL.class);
    static {
        PropertyConfigurator.configure("/home/yhb/coding/dayDayUp/BigData_mvn/conf/log4j.properties");
    }


    private static Configuration conf = null;
    private static Connection connection = null;
    private HTable hTable = null;
    // 参数设置
    public static final String HBASE_ZK_QUORUM="hbase.zookeeper.quorum";
    public static final String HBASE_ZK_QUORUM_VALUE="huabingood";
    public static final String HBASE_ZK_PROPERTY_CLIRNTPORT="hbase.zookeeper.property.clientPort";
    public static final String HBASE_ZK_PROPERTY_CLIRNTPORT_VALUE="2181";

    // 因为起一个获取配置信息然后创建Hbase连接是一个比较耗费的资源的过程
    // 因此，最好创建一个连接池，让大家共用，这里的ConnectionFactory实际上就是一个连接池
    // 这里的 static{} 实际上是对静态成员变量的显式声明
    static {
        conf = HBaseConfiguration.create();
        conf.set(HBASE_ZK_QUORUM,HBASE_ZK_QUORUM_VALUE);
        conf.set(HBASE_ZK_PROPERTY_CLIRNTPORT,HBASE_ZK_PROPERTY_CLIRNTPORT_VALUE);
        try {
            connection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建hbase中的表
     * 步骤：获取admin管理器；判断表是否存在；获取表的描述器；添加表的描述（列簇，属性）；创建表；关闭admin管理器
     * 创建成功返回 true
     * @return false
     */
    public static boolean myCreateTable(){
        boolean f = false;
        Admin admin = null;
        String tableName = "image1_test";
        String columnFamily1 = "cf1";
        String columnFamily2 = "cf2";

        try {
            // 创建habse管理器，由其专门处理DDL类的语言。
            // 这种方法在0.98后已经不再使用了，换成了conf.getAdmin()了
            // hBaseAdmin = new HBaseAdmin(connection);
            admin = connection.getAdmin();
            // 校验表是否存在
            if(admin.tableExists(TableName.valueOf(tableName))){
                myLog.error("表已经存在了！");
                System.err.println(tableName+"已经存在，创建失败！");
            }else{
                // 创建表
                HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
                // 往表中添加列
                tableDescriptor.addFamily(new HColumnDescriptor(columnFamily1));
                tableDescriptor.addFamily(new HColumnDescriptor(columnFamily2));
                // 开始真正执行创建表，也是懒加载
                admin.createTable(tableDescriptor);
                f= true;
            }

        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                // 关掉表管理器
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return f;
    }


    public boolean myDeleteTable(){
        boolean b = false;
        Admin admin = null;
        TableName tableName = TableName.valueOf("image1_test");

        try {
            // 跟HBase shell一样，需要先禁用表，然后再删除表
            admin = connection.getAdmin();
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            b = true;
            myLog.info("删除表成功。");
        } catch (IOException e) {
            e.printStackTrace();
            myLog.error("删除表失败");
        }

        return b;
    }



}
