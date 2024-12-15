import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;

public class HotelManagementApp {
    private JFrame mainFrame;
    private DefaultTableModel modelRooms, modelEmployees, modelGuests;
    private JButton save, add, delete, report, load;
    private JToolBar toolBar;
    private JScrollPane scrollRooms, scrollEmployees, scrollGuests;
    private JTable tableRooms, tableEmployees, tableGuests;
    private JTextField searchGuest;
    private JComboBox<String> filterComboBox;
    private JTabbedPane tabbedPane;
    private JTextArea textArea;
    private static final Logger logger = Logger.getLogger(HotelManagementApp.class);

    public void show() {
        logger.info("Starting Hotel Management Application.");
        mainFrame = new JFrame("Управление гостиницей");
        mainFrame.setSize(800, 600);
        mainFrame.setLocation(100, 100);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        save = new JButton("Сохранить");
        add = new JButton("Добавить");
        delete = new JButton("Удалить");
        report = new JButton("Отчет за месяц");
        load = new JButton("Загрузить");

        save.setToolTipText("Сохранить изменения");
        add.setToolTipText("Добавить запись");
        delete.setToolTipText("Удалить запись");
        load.setToolTipText("Загрузить данные из XML");

        toolBar = new JToolBar("Панель инструментов");
        toolBar.add(load);
        toolBar.add(save);
        toolBar.add(add);
        toolBar.add(delete);
        toolBar.add(report);
        toolBar.setFloatable(false);

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(toolBar, BorderLayout.NORTH);

        String[] columnsRooms = {"ID", "Вместимость", "Цена за ночь", "Доступность"};
        String[] columnsEmployees = {"ID", "ФИО", "Должность", "ID отеля"};
        String[] columnsGuests = {"ID", "ФИО", "ID комнаты", "Дата заезда", "Дата выезда"};

        modelRooms = new DefaultTableModel(columnsRooms, 0);
        modelEmployees = new DefaultTableModel(columnsEmployees, 0);
        modelGuests = new DefaultTableModel(columnsGuests, 0);

        tableRooms = new JTable(modelRooms);
        tableEmployees = new JTable(modelEmployees);
        tableGuests = new JTable(modelGuests);

        scrollRooms = new JScrollPane(tableRooms);
        scrollEmployees = new JScrollPane(tableEmployees);
        scrollGuests = new JScrollPane(tableGuests);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Номера", scrollRooms);
        tabbedPane.addTab("Сотрудники", scrollEmployees);
        tabbedPane.addTab("Гости", scrollGuests);

        mainFrame.add(tabbedPane, BorderLayout.CENTER);


        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        // Кнопка для многозадачности

        // Создание кнопки для многозадачности
        JButton multitaskingButton = new JButton("Многозадачность");
        multitaskingButton.addActionListener(e -> startMultitasking());  // Запуск многозадачности
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(multitaskingButton);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);


        mainFrame.setVisible(true);
        logger.debug("Application main frame and toolbar initialized.");

        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    saveXML();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Ошибка при сохранении: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    logger.warn("Error saving data to XML.");
                }
            }
        });

        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadXML();
            }
        });

        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    handleAdd();
                } catch (InvalidDataException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    handleDelete();
                } catch (NoRowSelectedException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        report.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Массив вариантов для выбора
                Object[] options = {"PDF", "HTML"};

                // Диалоговое окно для выбора формата отчета
                int choice = JOptionPane.showOptionDialog(mainFrame,
                        "Выберите формат отчета:",
                        "Выбор формата",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]); // Значение по умолчанию — PDF

                // Если выбрали PDF
                if (choice == 0) {
                    JOptionPane.showMessageDialog(mainFrame, "Генерация отчета за месяц в формате PDF...");
                    generatePDFReport();  // Вызов метода для генерации PDF
                }
                // Если выбрали HTML
                else if (choice == 1) {
                    JOptionPane.showMessageDialog(mainFrame, "Генерация отчета за месяц в формате HTML...");
                    generateHTMLReport();  // Вызов метода для генерации HTML
                }
                // Если пользователь не выбрал формат
                else {
                    JOptionPane.showMessageDialog(mainFrame, "Вы не выбрали формат отчета.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc); для особо важных случаев)
    public void generatePDFReport() {
        logger.info("Starting PDF report generation.");
        try {
            // Путь для сохранения PDF
            String fileName = "hotel_report.pdf";

            // Создаем PdfWriter и PdfDocument
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Загружаем шрифт с поддержкой кириллицы (например, Arial)
            PdfFont font = PdfFontFactory.createFont("C:\\Windows\\Fonts\\arial.ttf", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);

            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);

            // Заголовок отчета
            document.add(new Paragraph("Отчет за месяц")
                    .setBold()
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(font)
                    .setMarginBottom(20));

            // Номера: если нет данных, пишем, что номера отсутствуют
            document.add(new Paragraph("Номера:")
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFont(font)
                    .setMarginBottom(10));

            if (modelRooms.getRowCount() == 0) {
                logger.warn("No room data available.");
                document.add(new Paragraph("Номера отсутствуют").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setFont(font).setMarginBottom(10));
            } else {
                Table roomsTable = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                roomsTable.addHeaderCell(new Cell().add(new Paragraph("ID")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                roomsTable.addHeaderCell(new Cell().add(new Paragraph("Вместимость")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                roomsTable.addHeaderCell(new Cell().add(new Paragraph("Цена за ночь")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                roomsTable.addHeaderCell(new Cell().add(new Paragraph("Доступность")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));

                // Пример добавления строк для таблицы номеров
                for (int i = 0; i < modelRooms.getRowCount(); i++) {
                    logger.debug("Adding data to rooms table: row " + (i + 1));
                    roomsTable.addCell(new Cell().add(new Paragraph((String) modelRooms.getValueAt(i, 0))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    roomsTable.addCell(new Cell().add(new Paragraph((String) modelRooms.getValueAt(i, 1))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    roomsTable.addCell(new Cell().add(new Paragraph((String) modelRooms.getValueAt(i, 2))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    roomsTable.addCell(new Cell().add(new Paragraph((String) modelRooms.getValueAt(i, 3))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                }
                document.add(roomsTable.setMarginBottom(20));
            }

            // Сотрудники: если нет данных, пишем, что сотрудники отсутствуют
            document.add(new Paragraph("Сотрудники:")
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFont(font)
                    .setMarginBottom(10));

            if (modelEmployees.getRowCount() == 0) {
                logger.warn("No employee data available.");
                document.add(new Paragraph("Сотрудники отсутствуют").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setFont(font).setMarginBottom(10));
            } else {
                Table employeesTable = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                employeesTable.addHeaderCell(new Cell().add(new Paragraph("ID")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                employeesTable.addHeaderCell(new Cell().add(new Paragraph("ФИО")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                employeesTable.addHeaderCell(new Cell().add(new Paragraph("Должность")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                employeesTable.addHeaderCell(new Cell().add(new Paragraph("ID отеля")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));

                // Пример добавления строк для таблицы сотрудников
                for (int i = 0; i < modelEmployees.getRowCount(); i++) {
                    logger.debug("Adding data to employees table: row " + (i + 1));
                    employeesTable.addCell(new Cell().add(new Paragraph((String) modelEmployees.getValueAt(i, 0))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    employeesTable.addCell(new Cell().add(new Paragraph((String) modelEmployees.getValueAt(i, 1))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    employeesTable.addCell(new Cell().add(new Paragraph((String) modelEmployees.getValueAt(i, 2))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    employeesTable.addCell(new Cell().add(new Paragraph((String) modelEmployees.getValueAt(i, 3))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                }
                document.add(employeesTable.setMarginBottom(20));
            }

            // Гости: если нет данных, пишем, что гости отсутствуют
            document.add(new Paragraph("Гости:")
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFont(font)
                    .setMarginBottom(10));

            if (modelGuests.getRowCount() == 0) {
                logger.warn("No guest data available.");
                document.add(new Paragraph("Гости отсутствуют").setFontSize(12).setTextAlignment(TextAlignment.CENTER).setFont(font).setMarginBottom(10));
            } else {
                Table guestsTable = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();
                guestsTable.addHeaderCell(new Cell().add(new Paragraph("ID")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                guestsTable.addHeaderCell(new Cell().add(new Paragraph("ФИО")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                guestsTable.addHeaderCell(new Cell().add(new Paragraph("ID комнаты")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                guestsTable.addHeaderCell(new Cell().add(new Paragraph("Дата заезда")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));
                guestsTable.addHeaderCell(new Cell().add(new Paragraph("Дата выезда")).setBold().setTextAlignment(TextAlignment.CENTER).setFont(font).setFontSize(10));

                // Пример добавления строк для таблицы гостей
                for (int i = 0; i < modelGuests.getRowCount(); i++) {
                    logger.debug("Adding data to guests table: row " + (i + 1));
                    guestsTable.addCell(new Cell().add(new Paragraph((String) modelGuests.getValueAt(i, 0))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    guestsTable.addCell(new Cell().add(new Paragraph((String) modelGuests.getValueAt(i, 1))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    guestsTable.addCell(new Cell().add(new Paragraph((String) modelGuests.getValueAt(i, 2))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    guestsTable.addCell(new Cell().add(new Paragraph((String) modelGuests.getValueAt(i, 3))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                    guestsTable.addCell(new Cell().add(new Paragraph((String) modelGuests.getValueAt(i, 4))).setTextAlignment(TextAlignment.CENTER).setFont(font));
                }
                document.add(guestsTable);
            }

            // Закрытие документа
            document.close();
            logger.info("PDF report generation completed successfully.");
            JOptionPane.showMessageDialog(mainFrame, "Отчет в формате PDF успешно сгенерирован.");
        } catch (FileNotFoundException e) {
            logger.warn("Error in generating PDF report: ");
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при генерации PDF отчета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.warn("General error in generating PDF report: ");
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при генерации PDF отчета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generateHTMLReport() {
        logger.info("Starting HTML report generation.");
        try {
            // Загружаем XML-документ
            File xmlFile = new File("hotel_data.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Путь для сохранения HTML
            String fileName = "hotel_report.html";
            FileWriter writer = new FileWriter(fileName);

            // Начало HTML-документа
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"ru\">\n");
            writer.write("<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("<title>Отчет за месяц</title>\n");
            writer.write("<style>\n");
            writer.write("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n");
            writer.write("th, td { border: 1px solid black; padding: 8px; text-align: center; }\n");
            writer.write("th { background-color: #f2f2f2; font-weight: bold; }\n");
            writer.write("h1, h2 { text-align: center; font-size: 24px; }\n");
            writer.write("p { text-align: center; font-size: 18px; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("<h1>Отчет за месяц</h1>\n");

            // Номера
            writer.write("<h2>Номера:</h2>\n");
            NodeList roomsList = doc.getElementsByTagName("Room");
            if (roomsList.getLength() == 0) {
                writer.write("<p>Номера отсутствуют</p>\n");
            } else {
                writer.write("<table>\n");
                writer.write("<tr><th>ID</th><th>Вместимость</th><th>Цена за ночь</th><th>Доступность</th></tr>\n");
                for (int i = 0; i < roomsList.getLength(); i++) {
                    Element room = (Element) roomsList.item(i);
                    writer.write("<tr>");
                    writer.write("<td>" + room.getElementsByTagName("ID").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + room.getElementsByTagName("Capacity").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + room.getElementsByTagName("Price").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + room.getElementsByTagName("Availability").item(0).getTextContent() + "</td>");
                    writer.write("</tr>\n");
                }
                writer.write("</table>\n");
            }

            // Сотрудники
            writer.write("<h2>Сотрудники:</h2>\n");
            NodeList employeesList = doc.getElementsByTagName("Employee");
            if (employeesList.getLength() == 0) {
                writer.write("<p>Сотрудники отсутствуют</p>\n");
            } else {
                writer.write("<table>\n");
                writer.write("<tr><th>ID</th><th>ФИО</th><th>Должность</th><th>ID отеля</th></tr>\n");
                for (int i = 0; i < employeesList.getLength(); i++) {
                    Element employee = (Element) employeesList.item(i);
                    writer.write("<tr>");
                    writer.write("<td>" + employee.getElementsByTagName("ID").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + employee.getElementsByTagName("Name").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + employee.getElementsByTagName("Position").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + employee.getElementsByTagName("HotelID").item(0).getTextContent() + "</td>");
                    writer.write("</tr>\n");
                }
                writer.write("</table>\n");
            }

            // Гости
            writer.write("<h2>Гости:</h2>\n");
            NodeList guestsList = doc.getElementsByTagName("Guest");
            if (guestsList.getLength() == 0) {
                writer.write("<p>Гости отсутствуют</p>\n");
            } else {
                writer.write("<table>\n");
                writer.write("<tr><th>ID</th><th>ФИО</th><th>ID комнаты</th><th>Дата заезда</th><th>Дата выезда</th></tr>\n");
                for (int i = 0; i < guestsList.getLength(); i++) {
                    Element guest = (Element) guestsList.item(i);
                    writer.write("<tr>");
                    writer.write("<td>" + guest.getElementsByTagName("ID").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + guest.getElementsByTagName("Name").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + guest.getElementsByTagName("RoomID").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + guest.getElementsByTagName("CheckIn").item(0).getTextContent() + "</td>");
                    writer.write("<td>" + guest.getElementsByTagName("CheckOut").item(0).getTextContent() + "</td>");
                    writer.write("</tr>\n");
                }
                writer.write("</table>\n");
            }

            // Конец HTML-документа
            writer.write("</body>\n");
            writer.write("</html>\n");

            // Закрываем файл
            writer.close();
            logger.info("HTML report generation completed successfully.");
            JOptionPane.showMessageDialog(mainFrame, "Отчет в формате HTML успешно сгенерирован.");
        } catch (Exception e) {
            logger.warn("Error in generating HTML report: ", e);
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при генерации HTML отчета: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadXML() {
        try {
            File xmlFile = new File("hotel_data.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Очистка текущих данных и загрузка списка комнат
            NodeList roomsList = doc.getElementsByTagName("Room");
            modelRooms.setRowCount(0);
            for (int i = 0; i < roomsList.getLength(); i++) {
                Element room = (Element) roomsList.item(i);
                String id = room.getElementsByTagName("ID").item(0).getTextContent();
                String capacity = room.getElementsByTagName("Capacity").item(0).getTextContent();
                String price = room.getElementsByTagName("Price").item(0).getTextContent();
                String availability = room.getElementsByTagName("Availability").item(0).getTextContent();
                modelRooms.addRow(new String[]{id, capacity, price, availability});
            }

            // Очистка текущих данных и загрузка списка сотрудников
            NodeList employeesList = doc.getElementsByTagName("Employee");
            modelEmployees.setRowCount(0);
            for (int i = 0; i < employeesList.getLength(); i++) {
                Element employee = (Element) employeesList.item(i);
                String id = employee.getElementsByTagName("ID").item(0).getTextContent();
                String name = employee.getElementsByTagName("Name").item(0).getTextContent();
                String position = employee.getElementsByTagName("Position").item(0).getTextContent();
                String hotelId = employee.getElementsByTagName("HotelID").item(0).getTextContent();
                modelEmployees.addRow(new String[]{id, name, position, hotelId});
            }

            // Очистка текущих данных и загрузка списка гостей
            NodeList guestsList = doc.getElementsByTagName("Guest");
            modelGuests.setRowCount(0);
            for (int i = 0; i < guestsList.getLength(); i++) {
                Element guest = (Element) guestsList.item(i);
                String id = guest.getElementsByTagName("ID").item(0).getTextContent();
                String name = guest.getElementsByTagName("Name").item(0).getTextContent();
                String roomId = guest.getElementsByTagName("RoomID").item(0).getTextContent();
                String checkIn = guest.getElementsByTagName("CheckIn").item(0).getTextContent();
                String checkOut = guest.getElementsByTagName("CheckOut").item(0).getTextContent();
                modelGuests.addRow(new String[]{id, name, roomId, checkIn, checkOut});
            }

            JOptionPane.showMessageDialog(mainFrame, "Данные успешно загружены из XML.");
            logger.info("Data successfully loaded from XML.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            logger.warn("Error loading XML data.", ex);
        }
    }

    private void saveXML() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element rootElement = document.createElement("HotelData");
        document.appendChild(rootElement);

        // Сохранение комнат
        Element roomsElement = document.createElement("Rooms");
        rootElement.appendChild(roomsElement);
        for (int i = 0; i < modelRooms.getRowCount(); i++) {
            Element room = document.createElement("Room");
            roomsElement.appendChild(room);

            Element id = document.createElement("ID");
            id.appendChild(document.createTextNode(modelRooms.getValueAt(i, 0).toString()));
            room.appendChild(id);

            Element capacity = document.createElement("Capacity");
            capacity.appendChild(document.createTextNode(modelRooms.getValueAt(i, 1).toString()));
            room.appendChild(capacity);

            Element price = document.createElement("Price");
            price.appendChild(document.createTextNode(modelRooms.getValueAt(i, 2).toString()));
            room.appendChild(price);

            Element availability = document.createElement("Availability");
            availability.appendChild(document.createTextNode(modelRooms.getValueAt(i, 3).toString()));
            room.appendChild(availability);
        }

        // Сохранение сотрудников
        Element employeesElement = document.createElement("Employees");
        rootElement.appendChild(employeesElement);
        for (int i = 0; i < modelEmployees.getRowCount(); i++) {
            Element employee = document.createElement("Employee");
            employeesElement.appendChild(employee);

            Element id = document.createElement("ID");
            id.appendChild(document.createTextNode(modelEmployees.getValueAt(i, 0).toString()));
            employee.appendChild(id);

            Element name = document.createElement("Name");
            name.appendChild(document.createTextNode(modelEmployees.getValueAt(i, 1).toString()));
            employee.appendChild(name);

            Element position = document.createElement("Position");
            position.appendChild(document.createTextNode(modelEmployees.getValueAt(i, 2).toString()));
            employee.appendChild(position);

            Element hotelId = document.createElement("HotelID");
            hotelId.appendChild(document.createTextNode(modelEmployees.getValueAt(i, 3).toString()));
            employee.appendChild(hotelId);
        }

        // Сохранение гостей
        Element guestsElement = document.createElement("Guests");
        rootElement.appendChild(guestsElement);
        for (int i = 0; i < modelGuests.getRowCount(); i++) {
            Element guest = document.createElement("Guest");
            guestsElement.appendChild(guest);

            Element id = document.createElement("ID");
            id.appendChild(document.createTextNode(modelGuests.getValueAt(i, 0).toString()));
            guest.appendChild(id);

            Element name = document.createElement("Name");
            name.appendChild(document.createTextNode(modelGuests.getValueAt(i, 1).toString()));
            guest.appendChild(name);

            Element roomId = document.createElement("RoomID");
            roomId.appendChild(document.createTextNode(modelGuests.getValueAt(i, 2).toString()));
            guest.appendChild(roomId);

            Element checkIn = document.createElement("CheckIn");
            checkIn.appendChild(document.createTextNode(modelGuests.getValueAt(i, 3).toString()));
            guest.appendChild(checkIn);

            Element checkOut = document.createElement("CheckOut");
            checkOut.appendChild(document.createTextNode(modelGuests.getValueAt(i, 4).toString()));
            guest.appendChild(checkOut);
        }

        // Запись документа в файл
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File("hotel_data.xml"));
        transformer.transform(source, result);

        JOptionPane.showMessageDialog(mainFrame, "Данные успешно сохранены в XML.");
        logger.info("Data successfully saved to XML.");
    }

    private void editXML() {
        logger.info("Data in XML is editing.");
        try {
            // Открываем XML-файл и парсим его
            File xmlFile = new File("hotel_data.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Пример редактирования данных для комнат
            NodeList roomsList = doc.getElementsByTagName("Room");
            for (int i = 0; i < roomsList.getLength(); i++) {
                Element room = (Element) roomsList.item(i);

                // Например, изменим доступность каждой комнаты на "Occupied"
                Node availabilityNode = room.getElementsByTagName("Availability").item(0);
                if (availabilityNode != null) {
                    availabilityNode.setTextContent("Occupied");
                }

                // Также можно менять другие элементы, например, цену или вместимость
                Node priceNode = room.getElementsByTagName("Price").item(0);
                if (priceNode != null) {
                    priceNode.setTextContent("150"); // Пример изменения цены на 150
                }
            }

            // Запись изменений обратно в файл
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("hotel_data.xml"));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

            logger.info("Data successfully edited in XML.");
            // Сообщение об успешном редактировании
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainFrame, "Данные успешно отредактированы в XML.");
                textArea.append("Data edited in XML.\n");
            });
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, "Ошибка при редактировании XML: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE));
            logger.warn("Error editing data in XML.");
        }
    }

    private void startMultitasking() {
        // Поток для загрузки данных
        Thread loadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadXML();  // Вызываем ваш уже существующий метод
            }
        }, "LoadXMLThread");
        loadThread.start();

        // Поток для редактирования данных
        Thread editThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadThread.join();  // Ждем завершения потока загрузки данных
                    editXML();
                    SwingUtilities.invokeLater(() -> {
                        textArea.append("Data edited.\n");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "EditXMLThread");
        editThread.start();

        // Поток для генерации отчета
        Thread reportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    editThread.join();  // Ждем завершения редактирования данных
                    generateHTMLReport();  // Вызываем ваш уже существующий метод
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "ReportHTMLThread");
        reportThread.start();
    }

    private void checkDuplicateId(String id, String type) throws DuplicateIdException {
        DefaultTableModel model = null;
        String typeName = ""; // Название типа для вывода

        // Выбираем модель и название типа в зависимости от аргумента type
        if (type.equals("Room")) {
            model = modelRooms;
            typeName = "Комната";
        } else if (type.equals("Guest")) {
            model = modelGuests;
            typeName = "Гость";
        } else if (type.equals("Employee")) {
            model = modelEmployees;
            typeName = "Сотрудник";
        }

        // Проверяем на дублирование в соответствующей модели
        for (int i = 0; i < model.getRowCount(); i++) {
            String existingId = model.getValueAt(i, 0).toString();
            if (existingId.equals(id)) {
                throw new DuplicateIdException(typeName + " с таким ID уже существует.");
            }
        }
    }


    private void handleAdd() throws InvalidDataException {
        logger.debug("Add dialog opened with options: Room, Employee, Guest.");

        // Массив с вариантами для добавления (Комната, Сотрудник, Гость)
        String[] options = {"Комната", "Сотрудник", "Гость"};
        String choice = (String) JOptionPane.showInputDialog(mainFrame, "Что вы хотите добавить?",
                "Выбор добавления", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) {
            return; // Если пользователь закрыл диалог без выбора
        }

        switch (choice) {
            case "Комната":
                addRoom();
                break;
            case "Сотрудник":
                addEmployee();
                break;
            case "Гость":
                addGuest();
                break;
            default:
                logger.warn("Attempted to add unknown entry type.");
                throw new InvalidDataException("Неизвестный тип добавления.");
        }
        logger.info("Adding new entry of type: " + choice);
    }

    // Метод для добавления комнаты
    private void addRoom() throws InvalidDataException {
        JTextField idField = new JTextField();
        JTextField capacityField = new JTextField();
        JTextField priceField = new JTextField();
        JComboBox<String> availabilityComboBox = new JComboBox<>(new String[]{"Доступен", "Занят"});

        Object[] message = {
                "ID:", idField,
                "Вместимость:", capacityField,
                "Цена за ночь:", priceField,
                "Доступность:", availabilityComboBox
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Добавить комнату", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            String capacity = capacityField.getText();
            String price = priceField.getText();
            String availability = availabilityComboBox.getSelectedItem().toString();

            if (id.isEmpty() || capacity.isEmpty() || price.isEmpty()) {
                throw new InvalidDataException("Ошибка: все поля должны быть заполнены.");
            }

            // Проверка дублирования ID
            try {
                logger.debug("Checking for duplicate ID: " + id);
                checkDuplicateId(id, "Room");
                modelRooms.addRow(new String[]{id, capacity, price, availability});
                logger.info("Successfully added a new room entry with ID: " + id);
                JOptionPane.showMessageDialog(mainFrame, "Комната успешно добавлена.");
            } catch (DuplicateIdException ex) {
                logger.warn("Duplicate ID detected: " + id);
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Метод для добавления сотрудника
    private void addEmployee() throws InvalidDataException {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField positionField = new JTextField();
        JTextField hotelIdField = new JTextField();

        Object[] message = {
                "ID:", idField,
                "ФИО:", nameField,
                "Должность:", positionField,
                "ID отеля:", hotelIdField
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Добавить сотрудника", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            String name = nameField.getText();
            String position = positionField.getText();
            String hotelId = hotelIdField.getText();

            if (id.isEmpty() || name.isEmpty() || position.isEmpty() || hotelId.isEmpty()) {
                throw new InvalidDataException("Ошибка: все поля должны быть заполнены.");
            }

            // Проверка дублирования ID
            try {
                logger.debug("Checking for duplicate ID: " + id);
                checkDuplicateId(id, "Employee");
                modelEmployees.addRow(new String[]{id, name, position, hotelId});
                logger.info("Successfully added a new employee entry with ID: " + id);
                JOptionPane.showMessageDialog(mainFrame, "Сотрудник успешно добавлен.");
            } catch (DuplicateIdException ex) {
                logger.warn("Duplicate ID detected: " + id);
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Метод для добавления гостя
    private void addGuest() throws InvalidDataException {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField roomIdField = new JTextField();
        JTextField checkInField = new JTextField();
        JTextField checkOutField = new JTextField();

        Object[] message = {
                "ID:", idField,
                "ФИО:", nameField,
                "ID комнаты:", roomIdField,
                "Дата заезда (гггг-мм-дд):", checkInField,
                "Дата выезда (гггг-мм-дд):", checkOutField
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Добавить гостя", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            String name = nameField.getText();
            String roomId = roomIdField.getText();
            String checkIn = checkInField.getText();
            String checkOut = checkOutField.getText();

            if (id.isEmpty() || name.isEmpty() || roomId.isEmpty() || checkIn.isEmpty() || checkOut.isEmpty()) {
                throw new InvalidDataException("Ошибка: все поля должны быть заполнены.");
            }

            // Проверка дублирования ID
            try {
                logger.debug("Checking for duplicate ID: " + id);
                checkDuplicateId(id, "Guest");
                modelGuests.addRow(new String[]{id, name, roomId, checkIn, checkOut});
                logger.info("Successfully added a new guest entry with ID: " + id);
                JOptionPane.showMessageDialog(mainFrame, "Гость успешно добавлен.");
            } catch (DuplicateIdException ex) {
                logger.warn("Duplicate ID detected: " + id);
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * Метод для обработки удаления записи из активной вкладки.
     * Если не выбрана строка для удаления, выбрасывается исключение NoRowSelectedException.
     */
    private void handleDelete() throws NoRowSelectedException {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        JTable selectedTable;
        DefaultTableModel selectedModel;

        switch (selectedTabIndex) {
            case 0:
                selectedTable = tableRooms;
                selectedModel = modelRooms;
                break;
            case 1:
                selectedTable = tableEmployees;
                selectedModel = modelEmployees;
                break;
            case 2:
                selectedTable = tableGuests;
                selectedModel = modelGuests;
                break;
            default:
                throw new IllegalStateException("Неизвестная вкладка.");
        }

        int selectedRow = selectedTable.getSelectedRow();
        if (selectedRow == -1) {
            logger.warn("Attempted to delete entry with no selected row.");
            throw new NoRowSelectedException("Ошибка: выберите строку для удаления.");
        }

        int confirm = JOptionPane.showConfirmDialog(mainFrame, "Вы уверены, что хотите удалить эту запись?",
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            selectedModel.removeRow(selectedRow);
            logger.info("Successfully deleted entry from tab: " + selectedTabIndex + ", row: " + selectedRow);
            JOptionPane.showMessageDialog(mainFrame, "Запись удалена.");
        }
    }

    public static void main(String[] args) {
        new HotelManagementApp().show();
    }
}


// Класс исключения для обработки ошибок добавления
class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
// Класс исключения для обработки дублирования ID
class DuplicateIdException extends Exception {
    public DuplicateIdException(String message) {
        super(message);
    }
}


// Класс исключения для обработки ошибок удаления
class NoRowSelectedException extends Exception {
    public NoRowSelectedException(String message) {
        super(message);
    }
}
