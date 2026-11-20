import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Database access for transactions (table: transactions). */
public final class TransactionDAO {
    private TransactionDAO() { }

    // Saves the transaction AND the account's new balance together (both succeed or neither does)
    public static boolean save(String accountNumber, Transaction t, double newBalance) {
        String insert = "INSERT INTO transactions "
                      + "(transaction_id, account_number, txn_date, txn_type, amount) "
                      + "VALUES (?, ?, ?, ?, ?)";
        String update = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection con = DBConnection.connect()) {
            con.setAutoCommit(false);
            try (PreparedStatement ins = con.prepareStatement(insert);
                 PreparedStatement upd = con.prepareStatement(update)) {

                ins.setString(1, t.getTransactionId());
                ins.setString(2, accountNumber);
                ins.setDate(3, java.sql.Date.valueOf(t.getDate()));
                ins.setString(4, t.getType().name());
                ins.setDouble(5, t.getAmount());
                ins.executeUpdate();

                upd.setDouble(1, newBalance);
                upd.setString(2, accountNumber);
                upd.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database error (TransactionDAO.save): " + e.getMessage());
            return false;
        }
    }

    // Reads every transaction (oldest first) and adds it to its account (accounts must be loaded first)
    public static void loadAll(Bank bank) throws SQLException {
        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM transactions ORDER BY seq");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Account account = bank.findAccount(rs.getString("account_number"));
                if (account == null) continue;

                LocalDate date = rs.getDate("txn_date").toLocalDate();
                account.loadTransaction(new Transaction(
                        rs.getString("transaction_id"),
                        date,
                        TransactionType.valueOf(rs.getString("txn_type")),
                        rs.getDouble("amount")));
            }
        }
    }
}
