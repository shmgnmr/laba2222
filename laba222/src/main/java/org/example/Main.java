package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        JComboBox<String> sheetComboBox;
        JFrame frame = new JFrame("2");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JButton importButton = new JButton("Загрузить файл.xlsx ");
        importButton.setBounds(50, 50, 250, 50);
        frame.add(importButton);

        JButton exportButton = new JButton("Создать файл-результат");
        exportButton.setBounds(50, 200, 250, 50);
        frame.add(exportButton);

        sheetComboBox = new JComboBox<>();
        sheetComboBox.setBounds(50, 130, 200, 30);
        frame.add(sheetComboBox);

        JButton exitButton = new JButton("Выход из программы");
        exitButton.setBounds(50, 300, 250, 50);
        exitButton.addActionListener(e -> System.exit(0));
        frame.add(exitButton);

        frame.setVisible(true);

        importButton.addActionListener(new ActionListener() {
            HashMap<String, HashMap<String, Double>> mapArray = new HashMap<>();
            HashMap<String, Double> mapCovariance = new HashMap<>();
            boolean exportButtonListenerAdded = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try (FileInputStream fis = new FileInputStream(file);
                         XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                        // Очистка комбобокса и предыдущих обработчиков
                        sheetComboBox.removeAllItems();
                        for (ActionListener al : sheetComboBox.getActionListeners()) {
                            sheetComboBox.removeActionListener(al);
                        }

                        // Заполнение комбобокса новыми листами
                        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                            sheetComboBox.addItem(workbook.getSheetName(i));
                        }

                        sheetComboBox.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String selectedSheetName = (String) sheetComboBox.getSelectedItem();
                                XSSFSheet selectedSheet = workbook.getSheet(selectedSheetName);

                                int rows = selectedSheet.getPhysicalNumberOfRows();
                                if (rows < 2) {
                                    JOptionPane.showMessageDialog(null, "Лист пустой или содержит только заголовок", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }

                                Row headerRow = selectedSheet.getRow(0);
                                int cols = headerRow.getPhysicalNumberOfCells();
                                int[] columnRowCounts = new int[cols];

                                for (int i = 0; i < cols; i++) {
                                    for (int j = 1; j < rows; j++) {
                                        Row row = selectedSheet.getRow(j);
                                        if (row != null) {
                                            Cell cell = row.getCell(i);
                                            if (cell != null) {
                                                columnRowCounts[i]++;
                                            }
                                        }
                                    }
                                }

                                int rowCount = columnRowCounts[0];
                                for (int i = 1; i < cols; i++) {
                                    if (columnRowCounts[i] != rowCount) {
                                        JOptionPane.showMessageDialog(null, "Расчеты не могут быть выполнены, ошибка в выборках", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                }

                                HashMap<String, double[]> commonMap = InputModule.extractDataFromSheet(selectedSheet);

                                mapArray.clear();
                                for (String key : commonMap.keySet()) {
                                    mapArray.put(key, new HashMap<>());
                                }
                                for (String key : commonMap.keySet()) {
                                    Calculation calculationExample = new Calculation(mapArray.get(key), commonMap.get(key));
                                    mapArray.put(key, calculationExample.makeCalculation(mapArray.get(key), commonMap.get(key)));
                                }
                                mapCovariance = Covariancee.getCovariance(commonMap);

                                if (!exportButtonListenerAdded) {
                                    exportButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            ExportModule.writeExcelFile(mapArray, mapCovariance);
                                        }
                                    });
                                    exportButtonListenerAdded = true;
                                }
                            }
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        String errorMessage = "Ошибка при импорте и чтении данных из файла Excel";
                        JOptionPane.showMessageDialog(null, errorMessage, "Ошибка", JOptionPane.INFORMATION_MESSAGE);

                        // Очистка комбобокса и сброс состояния
                        sheetComboBox.removeAllItems();
                        mapArray.clear();
                        mapCovariance.clear();
                    }
                }
            }
        });
    }
}
