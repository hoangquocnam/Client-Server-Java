package Server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

public class UIServer extends JPanel {

  static ArrayList<ClientThread> _clientThreadList = new ArrayList<>();
  static ClientThread _selectedClient;
  static Folder _selectedFolder;

  static JPanel LEFT_PANEL;
  static JPanel RIGHT_PANEL;

  // Left : client list
  JLabel labelClientCounter;
  DefaultTableModel modelClient = new DefaultTableModel();
  JTable tableClient = new JTable();

  // Right: folder list of selected client
  JPanel controlPanel = new JPanel();

  // Right - top: folder list
  JPanel panelFolder = new JPanel();
  JButton btnBack = new JButton("<");
  JTable tableFolder = new JTable();
  DefaultTableModel modelFolder = new DefaultTableModel();
  JButton btnCreateFolder = new JButton("Create");
  JButton btnDeleteFolder = new JButton("Delete");
  JButton btnRenameFolder = new JButton("Rename");
  JButton btnSaveFolder = new JButton("Save");

  // Right - bottom: file list of selected folder
  JPanel panelFile = new JPanel();
  JTable tableFile = new JTable();
  DefaultTableModel modelFile = new DefaultTableModel();

  // Center: Filed editor of selected file

  void addEvents() {
    tableClient
      .getSelectionModel()
      .addListSelectionListener(
        new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent event) {
            if (tableClient.getSelectedRow() > -1) {
              int selectedRow = tableClient.getSelectedRow();
              int selectedClientId = Integer.parseInt(
                tableClient.getValueAt(selectedRow, 0).toString()
              );
              for (int i = 0; i < _clientThreadList.size(); i++) {
                ClientThread client = _clientThreadList.get(i);
                if (client.getClientId() == selectedClientId) {
                  _selectedClient = client;
                  break;
                }
              }
              ArrayList<Folder> clientFolders = ServerStateManage.getClientFolders(
                _selectedClient.getClientId()
              );
              folderListTable(clientFolders);
            }
          }
        }
      );
    // tableFolder
    //   .getSelectionModel()
    //   .addListSelectionListener(
    //     new ListSelectionListener() {
    //       public void valueChanged(ListSelectionEvent event) {
    //         if (tableFolder.getSelectedRow() > -1) {
    //           int selectedRow = tableFolder.getSelectedRow();
    //           int selectedFolderId = Integer.parseInt(
    //             tableFolder.getValueAt(selectedRow, 0).toString()
    //           );
    //           _selectedFolder = ServerStateManage.getFolder(selectedFolderId);
    //           ArrayList<File> folderFiles = _selectedFolder.getFiles();
    //           fileListTable(folderFiles);
    //         }
    //       }
    //     }
    //   );
  }

  void clientListTable() {
    modelClient = (DefaultTableModel) tableClient.getModel();
    modelClient.setColumnCount(0);
    modelClient.addColumn("Client Id");
    modelClient.addColumn("Port");
    modelClient.addColumn("Status");
    for (int i = 0; i < _clientThreadList.size(); i++) {
      ClientThread client = _clientThreadList.get(i);
      modelClient.addRow(
        new Object[] {
          client.getClientId(),
          client.getClientPort(),
          client.getClientStatus(),
        }
      );
    }
  }

  void folderListTable(ArrayList<Folder> folders) {
    modelFolder = (DefaultTableModel) tableFolder.getModel();
    modelFolder.setColumnCount(0);
    modelFolder.addColumn("Client Id");
    modelFolder.addColumn("Folder Id");
    modelFolder.addColumn("Name");
    for (int i = 0; i < folders.size(); i++) {
      Folder folder = folders.get(i);
      modelFolder.addRow(
        new Object[] { folder.clientId, folder.id, folder.name }
      );
    }
  }

  void prepareLeftPanel() {
    LEFT_PANEL = new JPanel();
    LEFT_PANEL.setLayout(new BoxLayout(LEFT_PANEL, BoxLayout.Y_AXIS));

    labelClientCounter = new JLabel("Client: " + _clientThreadList.size());

    tableClient.setModel(new DefaultTableModel());
    tableClient.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(
      tableClient,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    );
    scrollPane.setPreferredSize(new Dimension(300, 50));
    clientListTable();

    LEFT_PANEL.add(labelClientCounter);
    LEFT_PANEL.add(scrollPane);

    LEFT_PANEL.setBorder(
      new CompoundBorder(
        new TitledBorder("Client"),
        new EmptyBorder(new Insets(5, 5, 5, 5))
      )
    );
  }

  void prepareFolderUI() {
    folderListTable(new ArrayList<Folder>());
    panelFolder.setLayout(new BoxLayout(panelFolder, BoxLayout.Y_AXIS));
    tableFolder.setFillsViewportHeight(true);
    JScrollPane scrollPane = new JScrollPane(
      tableFolder,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    );
    panelFolder.add(scrollPane);
    panelFolder.setBorder(
      new CompoundBorder(
        new TitledBorder("Folder"),
        new EmptyBorder(new Insets(5, 5, 5, 5))
      )
    );
  }

  void prepareRightPanel() {
    RIGHT_PANEL = new JPanel();
    RIGHT_PANEL.setLayout(new BoxLayout(RIGHT_PANEL, BoxLayout.Y_AXIS));
    prepareFolderUI();

    controlPanel.add(btnBack);
    JPanel ButtonContainer = new JPanel(new FlowLayout());
    ButtonContainer.add(btnCreateFolder);
    ButtonContainer.add(btnSaveFolder);
    ButtonContainer.add(btnRenameFolder);
    ButtonContainer.add(btnDeleteFolder);
    controlPanel.add(ButtonContainer);
    tableFolder.setModel(new DefaultTableModel());

    RIGHT_PANEL.add(controlPanel);
    RIGHT_PANEL.add(panelFolder);
  }

  public UIServer(ArrayList<ClientThread> clientThreadList) {
    synchronized (this) {
      _clientThreadList = clientThreadList;
      setLayout(new BorderLayout());
      prepareLeftPanel();
      prepareRightPanel();

      add(LEFT_PANEL, BorderLayout.WEST);
      add(RIGHT_PANEL, BorderLayout.EAST);
      addEvents();
    }
  }

  public void updateClientList() {
    synchronized (this) {
      labelClientCounter.setText("Client: " + _clientThreadList.size());
      modelClient.setRowCount(0);
      clientListTable();
    }
  }

  public void updateFolderList() {
    synchronized (this) {
      modelFolder.setRowCount(0);
      if (_selectedClient == null) return;
      folderListTable(
        ServerStateManage.getClientFolders(_selectedClient.getClientId())
      );
    }
  }
}
