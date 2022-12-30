package Server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

public class UIServer {

  static ArrayList<ClientThread> clientList = new ArrayList<ClientThread>();
  static ServerSocket serverSocket;
  static int COUNTER_CLIENT = 0;

  public static JFrame window;
  public static JList<String> userList;
  public static DefaultTableModel clientModel;
  public static JTable table;

  public JButton btnDisconnect, btnSearch, btnRemoveClient;
  public JTextField message, jtf;
  public JLabel ipLabel;
  public JLabel portLabel;

  public UIServer(int port) {
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
    userList = new JList<String>();
    JScrollPane paneUser = new JScrollPane(userList);
    paneUser.setBounds(10, 110, 130, 320);

    window.add(paneUser);
    message = new JTextField();
    message.setBounds(0, 0, 0, 0);
    window.add(message);

    clientModel =
      new DefaultTableModel(ServerHelper.TABLE_HEADERS, 0) {
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };

    table = new JTable();
    table.setModel(clientModel);
    table.setAutoCreateRowSorter(true);
    final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
      clientModel
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
    createEvents();
    openSocket();
  }

  void openSocket() {
    try {
      serverSocket = new ServerSocket(ServerHelper.port);
    } catch (IOException e) {
      e.printStackTrace();
    }

    ServerHelper.printInfo("Server started on port " + ServerHelper.port);
    Socket clientSocket;
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        ServerHelper.printInfo(
          "New client " + clientSocket.getPort() + " connected!"
        );
        ClientThread client = new ClientThread(clientSocket, COUNTER_CLIENT);
        clientList.add(client);
        client.start();
        updateClientList();
        COUNTER_CLIENT++;
      } catch (Exception e) {
        ServerHelper.printError(e.getMessage());
      }
    }
  }

  void createEvents() {
    // choose user to send message
    userList.addListSelectionListener(
      new javax.swing.event.ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
          if (userList.getSelectedIndex() != -1) {
            btnRemoveClient.setEnabled(true);
            ClientThread client = clientList.get(userList.getSelectedIndex());
            boolean test = client.requestDirectory();
            System.out.println("test: " + test);
          } else {
            btnRemoveClient.setEnabled(false);
          }
        }
      }
    );
  }

  void updateClientList() {
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          String[] list = new String[clientList.size()];
          for (int i = 0; i < clientList.size(); i++) {
            list[i] = clientList.get(i)._name;
          }
          userList.setListData(list);
        }
      }
    );
  }
}
