
import java.math.BigDecimal;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class OrderUtils {
    public static final int CLIENT_NUM = 20;
    public static final int ITEM_NUM = 150;
    static Scanner scanner = new Scanner(System.in);

    public static void initTables() throws SQLException {
        Statement statement = OrdersRunner.connection.createStatement();
        try {
            statement.execute("ALTER TABLE orders DROP FOREIGN KEY FK_client");
            statement.execute("ALTER TABLE orders DROP FOREIGN KEY FK_item");
            statement.execute("DROP TABLE IF EXISTS clients");
            statement.execute("DROP TABLE IF EXISTS goods");
            statement.execute("DROP TABLE IF EXISTS orders");
            statement.execute("CREATE TABLE clients (" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "email VARCHAR(40) NOT NULL UNIQUE," +
                    "firstName VARCHAR (40) NOT NULL," +
                    "lastName VARCHAR (40) NOT NULL," +
                    "address VARCHAR(255) NOT NULL," +
                    "phone VARCHAR(25) NOT NULL," +
                    "PRIMARY KEY (id))");
            statement.execute("CREATE TABLE goods(" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "itemName VARCHAR (40) NOT NULL UNIQUE," +
                    "quantity INT," +
                    "price DOUBLE PRECISION," +
                    "PRIMARY KEY (id))");
            statement.execute("CREATE TABLE orders(" +
                    "id INT NOT NULL AUTO_INCREMENT," +
                    "date DATE NOT NULL," +
                    "PRIMARY KEY (id))");
            statement.execute("ALTER TABLE orders ADD COLUMN (item_id INT NOT NULL, quantity INT NOT NULL, client_id INT NOT NULL, total_price DOUBLE NOT NULL)");
            statement.execute("ALTER TABLE orders ADD CONSTRAINT FK_item FOREIGN KEY (item_id) REFERENCES goods(id)");
            statement.execute("ALTER TABLE orders ADD CONSTRAINT FK_client FOREIGN KEY (client_id) REFERENCES clients(id)");
        } finally {
            statement.close();
        }

        initRandomClients();
        initRandomGoods();
    }

    private static void initRandomGoods() throws SQLException {
        Random random = new Random();
        try {
            OrdersRunner.connection.setAutoCommit(false);
            try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("INSERT INTO goods (itemName, " +
                    "quantity, price) VALUES (?,?,?)")) {
                for (int i = 0; i < ITEM_NUM; i++) {
                    String itemName = new StringBuilder("Item").append(i + 1).toString();
                    Integer quantity = random.nextInt(300);
                    Double price = getRoundedDouble(random);
                    persistItem(ps, itemName, quantity, price);
                }
                OrdersRunner.connection.commit();
            } catch (Exception ex) {
                System.out.println("Items init fault!");
                OrdersRunner.connection.rollback();
            }
        } finally {
            OrdersRunner.connection.setAutoCommit(true);
        }
    }

    private static void persistItem(PreparedStatement ps, String itemName, Integer quantity, Double price) throws SQLException {
        ps.setString(1, itemName);
        ps.setInt(2, quantity);
        ps.setDouble(3, price);
        ps.executeUpdate();
    }

    private static void initRandomClients() throws SQLException {
        Random random = new Random();
        try {
            OrdersRunner.connection.setAutoCommit(false);
            try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("INSERT INTO clients (email, " +
                    "firstName, lastName, address, phone) VALUES (?,?,?,?,?)")) {
                for (int i = 0; i < CLIENT_NUM; i++) {
                    String email = new StringBuilder("email").append(i).append("@gmail.com").toString();
                    String firstName = new StringBuilder("Name").append(i).toString();
                    String lastName = new StringBuilder("Surname").append(i).toString();
                    String address = new StringBuilder("Kiev, Some str, build.").append(1 + random.nextInt(15))
                            .append(" apt.").append(1 + random.nextInt(200)).toString();
                    String phone = i < 10 ? ("555-28-0" + i) : ("555-28-" + i);
                    persistClient(ps, email, firstName, lastName, address, phone);
                }
                OrdersRunner.connection.commit();
            } catch (Exception ex) {
                System.out.println("Clients init fault!");
                OrdersRunner.connection.rollback();
            }
        } finally {
            OrdersRunner.connection.setAutoCommit(true);
        }
    }

    private static void persistClient(PreparedStatement ps, String email, String firstName, String lastName, String address, String phone) throws SQLException {
        ps.setString(1, email);
        ps.setString(2, firstName);
        ps.setString(3, lastName);
        ps.setString(4, address);
        ps.setString(5, phone);
        ps.executeUpdate();
    }

    private static double getRoundedDouble(Random random) {
        return new BigDecimal(random.nextInt(3000) + random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    static void getResults(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                System.out.print(md.getColumnName(i) + "\t\t");
            }
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.print(rs.getString(i) + "\t\t");
                }
                System.out.println();
            }
        }
    }

    static void listTable(String table) throws SQLException {
        PreparedStatement ps = OrdersRunner.connection.prepareStatement("SELECT * FROM " + table);
        getResults(ps);
    }

    static void editBases() throws SQLException {
        while (true) {
            System.out.println("1. Add client");
            System.out.println("2. Remove Client");
            System.out.println("3. Add item");
            System.out.println("4. Remove item");
            System.out.print("-> ");

            String line = scanner.nextLine();

            switch (line) {
                case ("1"):
                    addClient();
                    return;
                case ("2"):
                    removeFromTable(OrdersRunner.CLIENTS_TABLE);
                    return;
                case ("3"):
                    addItem();
                    return;
                case ("4"):
                    removeFromTable(OrdersRunner.GOODS_TABLE);
                    return;
                default:
                    return;
            }
        }
    }

    private static void addClient() throws SQLException {
        OrdersRunner.connection.setAutoCommit(false);
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("INSERT INTO clients (email, " +
                "firstName, lastName, address, phone) VALUES (?,?,?,?,?)")) {
            System.out.println("Enter client's email:");
            String email = scanner.nextLine();
            System.out.println("Enter client's first name:");
            String name = scanner.nextLine();
            System.out.println("Enter client's last name:");
            String lastName = scanner.nextLine();
            System.out.println("Enter client's address:");
            String address = scanner.nextLine();
            System.out.println("Enter client's phone:");
            String phone = scanner.nextLine();

            if (!email.isEmpty() && !name.isEmpty() && !lastName.isEmpty() && !address.isEmpty() && !phone.isEmpty()) {
                persistClient(ps, email, name, lastName, address, phone);
            } else {
                System.out.println("All fields must be filled!!!");
                throw new Exception();
            }
            OrdersRunner.connection.commit();
            System.out.println("Client added successfully!");
        } catch (Exception ex) {
            System.out.println("Client not added!");
            OrdersRunner.connection.rollback();
        } finally {
            OrdersRunner.connection.setAutoCommit(true);
        }
    }

    private static void addItem() throws SQLException {
        OrdersRunner.connection.setAutoCommit(false);
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("INSERT INTO goods (itemName, quantity, price) VALUES (?,?,?)")) {
            System.out.println("Enter item's name:");
            String itemName = scanner.nextLine();
            System.out.println("Enter the quantity of goods:");
            Integer quantity = scanner.nextInt();
            System.out.println("Enter the item's price:");
            Double price = scanner.nextDouble();
            if (itemName != null && quantity != 0 && price != 0) {
                persistItem(ps, itemName, quantity, price);
            } else {
                System.out.println("All fields must be filled!");
                throw new Exception();
            }
            OrdersRunner.connection.commit();
            System.out.println("Item added successful");
        } catch (Exception ex) {
            System.out.println("Item not added!");
            OrdersRunner.connection.rollback();
        } finally {
            OrdersRunner.connection.setAutoCommit(true);
        }
    }

    private static void removeFromTable(String table) throws SQLException {
        String result;
        if (table.equals(OrdersRunner.CLIENTS_TABLE)) {
            System.out.println("Enter email of client, you want to delete:");
            result = scanner.nextLine();
            try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("DELETE FROM clients WHERE email = ?")) {
                ps.setString(1, result);
                ps.executeUpdate();
            }
        } else if (table.equals(OrdersRunner.GOODS_TABLE)) {
            System.out.println("Enter the name of item, you want to delete:");
            result = scanner.nextLine();
            try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("DELETE FROM goods WHERE itemName = ?")) {
                ps.setString(1, result);
                ps.executeUpdate();
            }
        } else {
            System.out.println("Table does'n exist");
            return;
        }
        System.out.println("Delete successful");
    }

    static void createOrder() throws SQLException {
        System.out.println("Please enter the item name you want to buy:");
        String itemName = scanner.nextLine();
        int itemID = getItemID(itemName);
        if (itemID <= 0) {
            System.out.println("No such item!");
            return;
        }
        System.out.println("Please enter the quantity of items:");
        int quantity = scanner.nextInt();
        if (isEnough(itemID, quantity)) {
            System.out.println("Please enter customer's email:");
            String email = new Scanner(System.in).nextLine();
            Integer clientID = getClientID(email);
            Double price = getItemPrice(itemID) * quantity;
            if (clientID > 0) {
                putOrder(itemID, quantity, clientID, price);
                return;
            } else {
                System.out.println("There is no such client is DataBase! Do you want to create it? (y/n)");
                String ans = scanner.nextLine();
                if (ans.equalsIgnoreCase("y")) {
                    addClient();
                } else return;
            }
        } else {
            System.out.println("There's no items in the warehouse");
            return;
        }
    }

    private static int getItemID(String itemName) throws SQLException {
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("SELECT id FROM goods WHERE itemName = ?")) {
            ps.setString(1, itemName);
            String result = getResult(ps);
            Integer id = (result != null) ? Integer.parseInt(result) : (-1);
            if (id > 0) {
                return id;
            } else return 0;
        }
    }

    private static boolean isEnough(int itemID, int quantity) throws SQLException {
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("SELECT quantity FROM goods WHERE id = ?")) {
            ps.setInt(1, itemID);
            String result = getResult(ps);
            Integer dbQuantity = (result != null) ? Integer.parseInt(result) : (-1);
            if (dbQuantity > 0 && dbQuantity > quantity) {
                return true;
            } else return false;
        }
    }

    private static Integer getClientID(String email) throws SQLException {
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("SELECT id FROM clients WHERE email = ?")) {
            ps.setString(1, email);
            String result = getResult(ps);
            Integer id = (result != null) ? Integer.parseInt(result) : (-1);
            return id;
        }
    }

    private static double getItemPrice(int itemID) throws SQLException {
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("SELECT price FROM goods WHERE id = ?")) {
            ps.setInt(1, itemID);
            return Double.parseDouble(getResult(ps));
        }
    }

    private static void putOrder(int itemID, int quantity, Integer clientID, Double price) throws SQLException {
        OrdersRunner.connection.setAutoCommit(false);
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("INSERT INTO orders (date, item_id, quantity, client_id, total_price) VALUES (?,?,?,?,?)")) {
            ps.setDate(1, new Date(System.currentTimeMillis()));
            ps.setInt(2, itemID);
            ps.setInt(3, quantity);
            ps.setInt(4, clientID);
            ps.setDouble(5, price);
            ps.executeUpdate();
            OrdersRunner.connection.commit();
            updateItemsDB(itemID, quantity);
            System.out.println("Order created successfully!");
        } catch (Exception ex) {
            System.out.println("Order creation failed!");
            OrdersRunner.connection.rollback();
        } finally {
            OrdersRunner.connection.setAutoCommit(true);
        }
    }

    private static void updateItemsDB(int id, int quantity) throws SQLException {
        try (PreparedStatement ps = OrdersRunner.connection.prepareStatement("SELECT quantity FROM goods WHERE id = ?")) {
            ps.setInt(1, id);
            Integer dbQuantity = Integer.parseInt(getResult(ps));
            dbQuantity -= quantity;
            try (PreparedStatement ps1 = OrdersRunner.connection.prepareStatement("UPDATE goods SET quantity = ? WHERE id = ?")) {
                ps1.setInt(1, dbQuantity);
                ps1.setInt(2, id);
                ps1.executeUpdate();
            }

        }
    }

    private static String getResult(PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ex) {
            return null;
        }
        return null;
    }
}
