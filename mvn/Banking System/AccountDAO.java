import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Database access for accounts (table: accounts). */
public final class AccountDAO {
    private AccountDAO() { }

    // Returns true if saved
    public static boolean save(String customerId, Account account) {
        String sql = "INSERT INTO accounts "
                   + "(account_number, customer_id, account_type, branch, balance, "
                   + "employer_name, employer_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setString(2, customerId);
            ps.setString(3, account.getAccountType());
            ps.setString(4, account.getBranch());
            ps.setDouble(5, account.getBalance());
            if (account instanceof ChequeAccount) {
                ChequeAccount cheque = (ChequeAccount) account;
                ps.setString(6, cheque.getEmployerName());
                ps.setString(7, cheque.getEmployerAddress());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error (AccountDAO.save): " + e.getMessage());
            return false;
        }
    }

    // Reads every account and adds it to its customer (customers must be loaded first)
    public static void loadAll(Bank bank) throws SQLException {
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts ORDER BY account_number");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Customer owner = bank.findCustomer(rs.getString("customer_id"));
                if (owner == null) continue;

                String number  = rs.getString("account_number");
                String branch  = rs.getString("branch");
                double balance = rs.getDouble("balance");
                String type    = rs.getString("account_type");

                Account account;
                if ("Savings".equals(type)) {
                    account = new SavingsAccount(number, balance, branch);
                } else if ("Investment".equals(type)) {
                    account = new InvestmentAccount(number, balance, branch);
                } else {
                    account = new ChequeAccount(number, balance, branch,
                            rs.getString("employer_name"), rs.getString("employer_address"));
                }
                owner.addAccount(account);
            }
        }
    }
}
