import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Database access for customers (table: customers). */
public final class CustomerDAO {
    private CustomerDAO() { }

    // Returns true if saved
    public static boolean save(Customer customer) {
        String sql = "INSERT INTO customers "
                   + "(customer_id, customer_type, address, first_name, surname, company_name) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {          // Step 4

            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getCustomerType());
            ps.setString(3, customer.getAddress());
            if (customer instanceof IndividualCustomer) {
                IndividualCustomer ind = (IndividualCustomer) customer;
                ps.setString(4, ind.getFirstName());
                ps.setString(5, ind.getSurname());
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setNull(4, Types.VARCHAR);
                ps.setNull(5, Types.VARCHAR);
                ps.setString(6, ((CompanyCustomer) customer).getCompanyName());
            }
            ps.executeUpdate();                                          // Step 5
            return true;
        } catch (SQLException e) {
            System.out.println("Database error (CustomerDAO.save): " + e.getMessage());
            return false;
        }                                                                // Step 7
    }

    // Reads every customer from the database into the Bank's array
    public static void loadAll(Bank bank) throws SQLException {
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM customers ORDER BY customer_id");
             ResultSet rs = ps.executeQuery()) {                          // Step 5

            while (rs.next()) {                                           // Step 6
                String id      = rs.getString("customer_id");
                String address = rs.getString("address");
                Customer customer;
                if ("Individual".equals(rs.getString("customer_type"))) {
                    customer = new IndividualCustomer(id, address,
                            rs.getString("first_name"), rs.getString("surname"));
                } else {
                    customer = new CompanyCustomer(id, address, rs.getString("company_name"));
                }
                bank.addLoadedCustomer(customer);
            }
        }
    }
}
