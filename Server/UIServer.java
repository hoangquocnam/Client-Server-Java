package Server;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class UIServer {

  public static JFrame window;
  public static JList<String> user;
  public static DefaultTableModel jobsModel;
  public static JTable table;

  public JButton btnDisconnect, btnSearch, btnRemoveClient;
  public JTextField message, jtf;
  public JLabel ipLabel;
  public JLabel portLabel;

  public UIServer(int port) {
    // openSocket();
    window = new JFrame("Monitoring system");
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLayout(null);
    window.setBounds(200, 200, 1200, 480);
    window.setResizable(false);

    // port
    JLabel portText = new JLabel("Port:");
    portText.setBounds(20, 8, 60, 30);
    window.add(portText);

    portLabel = new JLabel(String.valueOf(port));
    portLabel.setFont(
      portLabel
        .getFont()
        .deriveFont(portLabel.getFont().getStyle() | java.awt.Font.BOLD)
    );
    portLabel.setBounds(50, 8, 70, 30);
    window.add(portLabel);

    // disconnect
    btnDisconnect = new JButton("Stop");
    btnDisconnect.setBounds(150, 10, 100, 30);
    window.add(btnDisconnect);

    // log button
    btnRemoveClient = new JButton("Remove client");
    btnRemoveClient.setBounds(200, 70, 200, 30);
    btnRemoveClient.setEnabled(false);
    window.add(btnRemoveClient);

    // client list
    JLabel label_text = new JLabel("List client");
    label_text.setBounds(20, 80, 100, 30);
    window.add(label_text);

    // scroll pane
    user = new JList<String>();
    JScrollPane paneUser = new JScrollPane(user);
    paneUser.setBounds(10, 110, 130, 320);

    window.add(paneUser);
    message = new JTextField();
    message.setBounds(0, 0, 0, 0);
    window.add(message);

    jobsModel =
      new DefaultTableModel(ServerHelper.TABLE_HEADERS, 0) {
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

    table = new JTable();
    table.setModel(jobsModel);
    table.setAutoCreateRowSorter(true);
    final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
      jobsModel
    );
    table.setRowSorter(sorter);
    table.setBounds(145, 110, 1030, 320);

    TableColumnModel columnModel = table.getColumnModel();
    columnModel.getColumn(0).setPreferredWidth(20);
    columnModel.getColumn(1).setPreferredWidth(150);
    columnModel.getColumn(2).setPreferredWidth(100);
    columnModel.getColumn(3).setPreferredWidth(100);
    columnModel.getColumn(4).setPreferredWidth(100);
    // adding it to JScrollPane
    JScrollPane sp = new JScrollPane(table);
    sp.setBounds(145, 110, 1030, 320);
    window.add(sp);
    window.setVisible(true);
  }
}
