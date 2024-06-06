import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ToDoListApp extends JFrame {
    private Connection connection;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField taskField;

    public ToDoListApp() {
        super("                    Aditya's To-Do ");

        // Initialize GUI components
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        JButton addTaskButton = new JButton("Add Task");
        JButton deleteTaskButton = new JButton("Delete Task");
        taskField = new JTextField();

        // Add action listeners
        addTaskButton.addActionListener(e -> addTask());
        deleteTaskButton.addActionListener(e -> deleteTask());

        // Add components to the frame
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(taskField, BorderLayout.CENTER);
        inputPanel.add(addTaskButton, BorderLayout.EAST);

        Container contentPane = getContentPane();
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(inputPanel, BorderLayout.NORTH);
        contentPane.add(deleteTaskButton, BorderLayout.SOUTH);

        // Connect to the database
        connectToDatabase();

        // Set up frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);

        // Load initial data
        loadTasks();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/todo";
            String user = "root";
            String password = "@#aditya2006";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void loadTasks() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tasks");
            tableModel.setColumnIdentifiers(new String[]{"ID", "Task", "Status"});
            tableModel.setRowCount(0); // Clear existing rows
            while (resultSet.next()) {
                tableModel.addRow(new Object[]{
                        resultSet.getInt("id"),
                        resultSet.getString("task"),
                        resultSet.getBoolean("status") ? "Completed" : "Pending"
                });
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to retrieve tasks.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTask() {
        String task = taskField.getText().trim();
        if (!task.isEmpty()) {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO tasks (task) VALUES (?)");
                statement.setString(1, task);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    taskField.setText("");
                    loadTasks();
                    JOptionPane.showMessageDialog(this, "Task added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add task.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Task cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int taskId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM tasks WHERE id = ?");
                statement.setInt(1, taskId);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    loadTasks();
                    JOptionPane.showMessageDialog(this, "Task deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete task.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to delete task.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListApp::new);
    }
}
