import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JFrame {

    //Creating Table and interface
    JTable j;

    private JTextField konuTextField;

    private JTextField noTextField;

    private JDatePickerImpl tarihBaslangicPicker;

    private JDatePickerImpl tarihBitisPicker;

    private JTextField maddeTextField;

    private static Connection conn;

//    private String[][] data = {};

    private String[] columns = new String[]{
            "Dosya İsmi", "Konu", "Karar No", "Karar Tarihi", "Karar Metni"
    };

    // creating panels interface
    Main() {
        JPanel sorgulaButtonPanel = new JPanel();
        JPanel sorguPanel = new JPanel();
        sorguPanel.setBorder(new EmptyBorder(10, 80, 10, 80));
        sorguPanel.setLayout(new GridLayout(6, 2));

        JLabel konuLabel, maddeLabel, noLabel, tarihBaslangicLabel, tarihBitisLabel;

        konuLabel = new JLabel("Konu");
        maddeLabel = new JLabel("Karar Metni");
        noLabel = new JLabel("Karar No");
        tarihBaslangicLabel = new JLabel("Tarih Başlangıç");
        tarihBitisLabel = new JLabel("Tarih Bitiş");

        konuTextField = new JTextField(100);
        maddeTextField = new JTextField(100);
        noTextField = new JTextField(100);
        ((AbstractDocument) noTextField.getDocument()).setDocumentFilter(new DocumentFilter() {
            Pattern regEx = Pattern.compile("\\d*");

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                Matcher matcher = regEx.matcher(text);
                if (!matcher.matches()) {
                    return;
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });

        UtilDateModel tarihBaslangicModel = new UtilDateModel();
        Properties tarihBaslangicProp = new Properties();
        tarihBaslangicProp.put("text.today", "Today");
        tarihBaslangicProp.put("text.month", "Month");
        tarihBaslangicProp.put("text.year", "Year");
        JDatePanelImpl tarihBaslangicPanel = new JDatePanelImpl(tarihBaslangicModel, tarihBaslangicProp);
        tarihBaslangicPicker = new JDatePickerImpl(tarihBaslangicPanel, new DateLabelFormatter());

        UtilDateModel tarihBitisModel = new UtilDateModel();
        Properties tarihBitisProp = new Properties();
        tarihBitisProp.put("text.today", "Today");
        tarihBitisProp.put("text.month", "Month");
        tarihBitisProp.put("text.year", "Year");
        JDatePanelImpl tarihBitisPanel = new JDatePanelImpl(tarihBitisModel, tarihBitisProp);
        tarihBitisPicker = new JDatePickerImpl(tarihBitisPanel, new DateLabelFormatter());

        JButton sorgulaButton = new JButton("Sorgula");
        sorgulaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    query();
                } catch (SQLException throwables) {

                    throwables.printStackTrace();
                }
            }
        });

        sorguPanel.add(konuLabel);
        sorguPanel.add(konuTextField);
        sorguPanel.add(noLabel);
        sorguPanel.add(noTextField);
        sorguPanel.add(tarihBaslangicLabel);
        sorguPanel.add(tarihBaslangicPicker);
        sorguPanel.add(tarihBitisLabel);
        sorguPanel.add(tarihBitisPicker);
        sorguPanel.add(maddeLabel);
        sorguPanel.add(maddeTextField);

        sorgulaButtonPanel.add(sorgulaButton);

        add(sorguPanel, "North");
        sorguPanel.add(sorgulaButtonPanel);

        JPanel listePanel = new JPanel();

        final DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
// tıkladığında aç
        j = new JTable(tableModel);
        j.setRowHeight(30);
        j.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(new File((String) tableModel.getValueAt(table.getSelectedRow(), 0)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // adding it to JScrollPane
        JScrollPane sp = new JScrollPane(j);
        sp.setPreferredSize(new Dimension(990, 500));
        listePanel.add(sp);

        add(listePanel);


        // Function to set visible
        // status of JFrame.
        setVisible(true);

        // this Keyword refers to current
        // object. Function to set size of JFrame.
        this.setSize(1200, 1000);
    }

    public void query() throws SQLException {
        DefaultTableModel tableModel = (DefaultTableModel) j.getModel();
        int foovalue = 500;


        String sql = "SELECT * FROM karar k WHERE 1 = 1 ";
        if (konuTextField.getText() != null && !konuTextField.getText().equals("")) {
            sql += "and lower(k.konu) like N\'%" + konuTextField.getText().toLowerCase() + "%\' ";
        }

        if (noTextField.getText() != null && !noTextField.getText().equals("")) {
            sql += "and lower(k.no) like N\'%" + noTextField.getText().toLowerCase() + "%\' ";
        }

        if (tarihBaslangicPicker.getJFormattedTextField().getText() != null && !tarihBaslangicPicker.getJFormattedTextField().getText().equals("")) {
            String tarihBaslangic = tarihBaslangicPicker.getJFormattedTextField().getText();
            tarihBaslangic = tarihBaslangic.replaceAll("-", "");
            sql += "and k.tarih >= \'" + tarihBaslangic + "\' ";
        }

        if (tarihBitisPicker.getJFormattedTextField().getText() != null && !tarihBitisPicker.getJFormattedTextField().getText().equals("")) {
            String tarihBitis = tarihBitisPicker.getJFormattedTextField().getText();
            tarihBitis = tarihBitis.replaceAll("-", "");
            sql += "and k.tarih <= \'" + tarihBitis + "\' ";
        }

        if (maddeTextField.getText() != null && !maddeTextField.getText().equals("")) {
            sql += "and lower(k.madde) like \'%" + maddeTextField.getText().toLowerCase() + "%\' ";
        }

        CallableStatement call = conn.prepareCall(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = call.executeQuery();
        tableModel.setRowCount(0);
        j.setModel(tableModel);
        while (rs.next()) {
            String[] data = new String[5];
            data[0] = rs.getString(2);
            data[1] = rs.getString(3);
            data[2] = rs.getString(5);
            data[3] = rs.getString(6);
            data[4] = rs.getString(4);
            data[3] = data[3].substring(0,4) + "-" + data[3].substring(4,6) + "-" + data[3].substring(6);
            tableModel.addRow(data);
        }
        j.setModel(tableModel);
        rs.close();
        call.close();
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres");
        conn = DriverManager.getConnection(url, props);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }

}
