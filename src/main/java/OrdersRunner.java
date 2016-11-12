import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class OrdersRunner {
    public static final String GOODS_TABLE = "goods";
    public static final String CLIENTS_TABLE = "clients";
    public static final String ORDERS_TABLE = "orders";
    static Connection connection;

    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        DBProperties props = new DBProperties();
        try {
            connection = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
            OrderUtils.initTables();

            while (true) {
                System.out.println("1. List goods");
                System.out.println("2. List clients");
                System.out.println("3. List orders");
                System.out.println("4. Edit goods/clients");
                System.out.println("5. Create order");
                System.out.print("-> ");

                String line = scanner.nextLine();

                switch (line) {
                    case ("1"):
                        OrderUtils.listTable(GOODS_TABLE);
                        break;
                    case ("2"):
                        OrderUtils.listTable(CLIENTS_TABLE);
                        break;
                    case ("3"):
                        OrderUtils.listTable(ORDERS_TABLE);
                        break;
                    case ("4"):
                        OrderUtils.editBases();
                        break;
                    case ("5"):
                        OrderUtils.createOrder();
                        break;
                    default:
                        return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            if (connection!=null) connection.close();
        }
    }


}