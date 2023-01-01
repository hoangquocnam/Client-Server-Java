package Server;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class UIServer {

  static ArrayList<ClientThread> clientList = new ArrayList<ClientThread>();
  static ServerSocket serverSocket;
  static int COUNTER_CLIENT = 0;

  public static JFrame window;
  public static JList<String> userList;
  public static JList<String> folderUserList;
  public static DefaultTableModel clientModel;
  public static JTable table;

  public JButton btnDisconnect, btnChangeFolder, btnRemoveClient;
  public JTextField message, jtf;
  public JLabel ipLabel;
  public JLabel portLabel;

  public UIServer(int port) {
    window = new JFrame("Monitoring system");
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLayout(null);
    window.setBounds(200, 200, 1500, 480);
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

    // change folder button
    btnRemoveClient = new JButton("Remove client");
    btnRemoveClient.setBounds(20, 70, 200, 30);
    window.add(btnRemoveClient);

    btnChangeFolder = new JButton("Change folder");
    btnChangeFolder.setBounds(220, 70, 200, 30);
    window.add(btnChangeFolder);

    // scroll pane
    userList = new JList<String>();
    JScrollPane paneUser = new JScrollPane(userList);
    paneUser.setBounds(10, 110, 130, 320);
    paneUser.setBorder(
      new CompoundBorder(
        new TitledBorder("Client"),
        new EmptyBorder(new Insets(5, 5, 5, 5))
      )
    );
    window.add(paneUser);

    folderUserList = new JList<String>();
    JScrollPane paneFolder = new JScrollPane(folderUserList);
    paneFolder.setBounds(150, 110, 250, 320);
    paneFolder.setBorder(
      new CompoundBorder(
        new TitledBorder("Folder"),
        new EmptyBorder(new Insets(5, 5, 5, 5))
      )
    );
    window.add(paneFolder);

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
    table.setBounds(410, 110, 1030, 320);

    TableColumnModel columnModel = table.getColumnModel();
    columnModel.getColumn(0).setPreferredWidth(150);
    columnModel.getColumn(1).setPreferredWidth(100);
    columnModel.getColumn(2).setPreferredWidth(100);
    columnModel.getColumn(3).setPreferredWidth(100);

    table.setEnabled(false);
    // adding it to JScrollPane
    JScrollPane sp = new JScrollPane(table);
    sp.setBounds(410, 110, 1030, 320);
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
    userList.addListSelectionListener(
      new javax.swing.event.ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
          if (userList.getSelectedIndex() != -1) {
            ClientThread client = clientList.get(userList.getSelectedIndex());
            ArrayList<String> list = client._foldersPath;
            if (client._selectedPathIndex == -1) {
              updateFolderList(list);
            } else {
              updateFolderList(new ArrayList<String>());
            }
          }
        }
      }
    );

    folderUserList.addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
          if (folderUserList.getSelectedIndex() != -1) {
            ClientThread client = clientList.get(userList.getSelectedIndex());
            if (client._selectedPathIndex == -1) {
              client._selectedPathIndex = folderUserList.getSelectedIndex();
              insertTable(
                folderUserList.getSelectedValue(),
                "WATCH",
                client._name
              );
              client.send("got%" + folderUserList.getSelectedValue());
              clearFolderList();
            }
          }
        }
      }
    );

    btnChangeFolder.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            if (userList.getSelectedIndex() != -1) {
              ClientThread client = clientList.get(userList.getSelectedIndex());
              if (client._selectedPathIndex != -1) {
                insertTable(
                  client._foldersPath.get(client._selectedPathIndex),
                  "STOP",
                  client._name
                );
                client._selectedPathIndex = -1;
                updateFolderList(client._foldersPath);
              }
              client.send("rmv%");
            }
          } catch (Exception ex) {
            ServerHelper.printError(ex.getMessage());
          }
        }
      }
    );

    btnRemoveClient.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            if (userList.getSelectedIndex() == -1) return;
            ClientThread client = clientList.get(userList.getSelectedIndex());
            if (client._selectedPathIndex != -1) {
              insertTable(
                client._foldersPath.get(client._selectedPathIndex),
                "STOP",
                client._name
              );
            }
            client.close();
            clientList.remove(userList.getSelectedIndex());
            updateClientList();
            updateFolderList(new ArrayList<String>());
          } catch (Exception ex) {
            ServerHelper.printError(ex.getMessage());
          }
        }
      }
    );

    btnDisconnect.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for (int i = 0; i < clientList.size(); i++) {
            ClientThread client = clientList.get(i);
            client.close();
            if (client._selectedPathIndex != -1) {
              insertTable(
                client._foldersPath.get(client._selectedPathIndex),
                "STOP",
                client._name
              );
            }
          }
          clientList.clear();
          updateClientList();
          updateFolderList(new ArrayList<String>());
          System.exit(0);
        }
      }
    );
  }

  static void updateClientList() {
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

  static void updateFolderList(ArrayList<String> list) {
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          folderUserList.setListData(list.toArray(new String[0]));
        }
      }
    );
  }

  static void clearFolderList() {
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          folderUserList.setListData(new String[0]);
        }
      }
    );
  }

  static void insertTable(String fileName, String kind, String clientName) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    String time = dtf.format(now);
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          clientModel.addRow(new Object[] { time, fileName, kind, clientName });
        }
      }
    );
  }

  static void removeClient(ClientThread client) {
    SwingUtilities.invokeLater(
      new Runnable() {
        public void run() {
          clientList.remove(client);
          updateClientList();
        }
      }
    );
  }
}
