package com.example;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.IntelliJTheme;

public class CustomTable {    
    public static void main(String[] args) {
        LafManager.installTheme(new IntelliJTheme());
        LafManager.install();
        // Schedule a job for the event dispatch thread: creating and showing this application's GUI.
        System.out.println("Starting");
        SwingUtilities.invokeLater(CustomTable::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("Custom Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);

        // Create a table model and set it to the JTable.
        DefaultTableModel model = new DefaultTableModel(new Object[3][3], new String[3]) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // All cells are editable
            }
        };
        JTable table = new JTable(model);

        // Add the table to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        for (int i = 0; i < model.getColumnCount(); i++) {
            sorter.setComparator(i, comparator);
        }
        table.setRowSorter(sorter);

        // Create buttons for save and load actions.
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton clearButton = new JButton("Clear Table");
        JButton addRowButton = new JButton("Add Row");
        JButton removeRowButton = new JButton("Remove Row");
        JButton addColumnButton = new JButton("Add Column");
        JButton removeColumnButton = new JButton("Remove Column");

        // Add action listeners to the buttons.
        saveButton.addActionListener(e -> { saveTableData(table); });
        loadButton.addActionListener(e -> { 
            loadTableData(model);
            // Re-enabling sorting since loading a new table removes this property
            for (int i = 0; i < model.getColumnCount(); i++) {
                sorter.setComparator(i, comparator);
            }
            table.setRowSorter(sorter);
        });
        clearButton.addActionListener(e -> { clearTableData(model); });
        addRowButton.addActionListener(e -> { addRow(model); });
        removeRowButton.addActionListener(e -> { removeRow(table, model); });
        addColumnButton.addActionListener(e -> { 
            addColumn(model);
            sorter.setComparator(model.getColumnCount() - 1, comparator); // Enable sorting on new column
        });
        removeColumnButton.addActionListener(e -> { removeColumn(table, model); });

        // Add mouse listener to the table header
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int col = table.columnAtPoint(e.getPoint());
                    showHeaderPopupMenu(e, table, col);
                }
            }
        });

        // Add the buttons to a panel.
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(addRowButton);
        buttonPanel.add(removeRowButton);
        buttonPanel.add(addColumnButton);
        buttonPanel.add(removeColumnButton);

        // Add the scroll pane and button panel to the frame.
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void saveTableData(JTable table) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
    
        // Set initial directory to the project location
        String projectLocation = System.getProperty("user.dir");
        fileChooser.setCurrentDirectory(new File(projectLocation));

        // Show save dialog; this will display the native file dialog on Windows 10
        int userSelection = fileChooser.showSaveDialog(null);
    
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
    
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave))) {
                for (int k = 0; k < table.getColumnCount(); k++) {
                    bw.write(table.getColumnName(k));
                    if (k < table.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        bw.write(table.getValueAt(i, j).toString());
                        if (j < table.getColumnCount() - 1) {
                            bw.write(",");
                        }
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(null, "Data saved successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving data.");
            }
        }
    }


    private static void loadTableData(DefaultTableModel model) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose a file to load");
    
        // Set initial directory to the project location
        String projectLocation = System.getProperty("user.dir");
        fileChooser.setCurrentDirectory(new File(projectLocation));
    
        // Show open dialog; this will display the native file dialog on Windows 10
        int userSelection = fileChooser.showOpenDialog(null);
    
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
    
            try (BufferedReader br = new BufferedReader(new FileReader(fileToLoad))) {
                model.setRowCount(0); // Clear existing rows
                model.setColumnCount(0); // Clear existing columns

                String headerLine;
                while ((headerLine = br.readLine()) != null) {
                    String[] columnNames = headerLine.split(",");
                    // Clear existing rows and columns
                    model.setRowCount(0);
                    model.setColumnIdentifiers(columnNames);
                    
                    // Read remaining lines as data
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");
                        model.addRow(values);
                    }
                }
                JOptionPane.showMessageDialog(null, "Data loaded successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading data.");
            }
        }
    }


    private static void clearTableData(DefaultTableModel mainModel) {
        int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the table?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            mainModel.setRowCount(0); // Used to clear first row
            mainModel.setRowCount(1);
        }
    }


    private static void addRow(DefaultTableModel mainModel) {
        // Create a new row with default values
        Object[] defaultMainRow = new Object[mainModel.getRowCount()];
        mainModel.addRow(defaultMainRow);
    }


    private static void removeRow(JTable mainTable, DefaultTableModel mainModel) {
        int selectedRow = mainTable.getSelectedRow();
        if (selectedRow != -1) {
            mainModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(null, "No row selected for removal.");
        }
    }


    private static void addColumn(DefaultTableModel model) {
        String columnName = JOptionPane.showInputDialog("Enter column name:");
        if (columnName != null && !columnName.isEmpty()) {
            model.addColumn(columnName);
        }
    }
    

    private static void removeColumn(JTable table, DefaultTableModel model) {
        int selectedColumn = table.getSelectedColumn();
        int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove the select row?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            int columnCount = table.getColumnCount();
            if (columnCount > 0) {
                if (selectedColumn != -1) {
                    model.setColumnCount(columnCount - 1);
                } else {
                    JOptionPane.showMessageDialog(null, "No column selected for removal.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "No columns to remove.");
            }
        }
    }
    

    private static void showHeaderPopupMenu(MouseEvent e, JTable table, int col) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem renameColumnItem = new JMenuItem("Rename Column  ");
        renameColumnItem.addActionListener(ev -> renameColumn(table, col));
        popupMenu.add(renameColumnItem);
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    

    private static void renameColumn(JTable table, int col) {
        String newName = JOptionPane.showInputDialog("Enter new column name:");
        if (newName != null && !newName.isEmpty()) {
            table.getColumnModel().getColumn(col).setHeaderValue(newName);
            table.getTableHeader().repaint();
        }
    }

            
    // Comparator used to sort columns
    public static Comparator<Object> comparator = (o1, o2) -> {
        if (o1.toString().matches("[0-9]*[A-zÀ-ú]+[0-9]*") && o2.toString().matches("[0-9]*[A-zÀ-ú]+[0-9]*")) {
            return o1.toString().compareTo(o2.toString()); // Ascending order
        } else {
            Integer int1 = Integer.valueOf(o1.toString());
            Integer int2 = Integer.valueOf(o2.toString());
            return int2.compareTo(int1); //Descending order
        }
    };

}
