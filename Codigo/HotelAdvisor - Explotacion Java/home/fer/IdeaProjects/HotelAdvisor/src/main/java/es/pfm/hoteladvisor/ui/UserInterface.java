package es.pfm.hoteladvisor.ui;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
//import dnl.utils.text.table.TextTable;
import es.pfm.hoteladvisor.HotelAdvisor;
import es.pfm.hoteladvisor.db.DbUtils;
import es.pfm.hoteladvisor.db.QueryHelper;
import es.pfm.hoteladvisor.model.Hotel;
import es.pfm.hoteladvisor.utils.Maths;
import es.pfm.hoteladvisor.utils.PoisTypes;
import org.bson.Document;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserInterface {

    private static UserInterface ui = null;
    protected UserInterface() {

    }
    public static UserInterface getInstance() {
        if(ui == null) {
            ui = new UserInterface();
        }
        return ui;
    }

    public void showHotels(int numRows, ArrayList<Hotel> hotelList, MongoCollection<Document> collectionPois, MongoCollection<Document> collectionUsers, String currentUser, boolean showDistance){
        Object [][] data  = new Object[numRows][];
        String columnNames[] = {"Hotel", "Image", "Score", "Comments","Category(*)", "Price(EUR)", "Rooms", "City", "Description", "POIs", "Distance (m)", "Hotel URL", "Hotel Id", "Longitude", "Latitude", "Current User"};
        int i = 0;

        for (Hotel hotel: hotelList) {

            Object[] row = new Object[columnNames.length];
            row[0] = hotel.getName();
            ImageIcon imageIcon = null;
            try {
                URL url = new URL(hotel.getPhoto_url());
                Image img = ImageIO.read(url);
                Image resizedImage = img.getScaledInstance(250, 250, 0);
                imageIcon = new ImageIcon(resizedImage);
            }catch (Exception e){
                final String dir = System.getProperty("user.dir");
                String imagePath;
                if (dir.contains("target"))
                    imagePath = "../src/main/resources/not_available.png";
                else
                    imagePath = "./src/main/resources/not_available.png";
                imageIcon = new ImageIcon(imagePath);
            }

            row[1] = imageIcon;
            row[2] = Maths.round(hotel.getReview_score(),2);
            row[3] = hotel.getReview_nr();
            row[4] = hotel.getClazz();
            Double min_rate_EUR = hotel.getMin_rate_EUR();
            if (min_rate_EUR!=null)
                min_rate_EUR = Maths.round(min_rate_EUR,2);
            else
                min_rate_EUR =0.0d;
            row[5] = min_rate_EUR;
            row[6]= hotel.getNr_rooms();
            row[7]=hotel.getCity();

            String desc = hotel.getDesc_es();

            FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
            Iterator userIterator = userIt.iterator();
            String userLang = "es";

            if (userIterator.hasNext()) {
                Document user = (Document) userIterator.next();
                userLang = user.getString("language");
            }

            if("en".equals(userLang))
                desc = hotel.getDesc_en();

            row[8] = desc;

            ArrayList<Double> coords = (ArrayList) hotel.getCoord();
            double longitude = coords.get(0);
            double latitude = coords.get(1);

            row[9] = "Show POIS";

            double distance = 0d;

            if(hotel.getDistance()!=null)
                distance = hotel.getDistance();

            row[10] = Maths.round(distance,2);;

            row[11] = hotel.getHotel_url();

            row[12] = hotel.getId();

            row[13] = longitude;

            row[14] = latitude;

            row[15] = currentUser;

            data[i] = row;

            i++;
        }

        //TextTable textTable = new TextTable(columnNames, data);
        //textTable.printTable();

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public Class getColumnClass(int column)
            {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        final JFrame frame = new JFrame();
        frame.setTitle("Hotel Advisor - Recommended hotels matching your search: ");

        for (int j=0;j<columnNames.length;j++) {
            tableModel.addColumn(columnNames[j]);
        }

        for (int j=0;j<data.length;j++) {
            tableModel.addRow(data[j]);
        }

        JTable table = new JTable(tableModel){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
                                             int columnIndex) {
                JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                if(columnIndex == 2) {
                    double value = (Double) getValueAt(rowIndex,columnIndex);
                    double numComments = (Double) getValueAt(rowIndex,columnIndex);
                    if (value==0 && numComments ==0)
                        component.setBackground(new Color(255, 255, 255));
                    else if (value<=5)
                        component.setBackground(new Color(255, 8, 25));
                    else if(value<7)
                        component.setBackground(new Color(255, 185, 23));
                    else if(value<8)
                        component.setBackground(new Color(180, 255, 20));
                    else if(value<9)
                        component.setBackground(new Color(34, 255, 14));
                    else
                        component.setBackground(new Color(8, 255, 140));
                }
                else
                    component.setBackground(new Color (255,255,255));

                return component;
            }
        };

        //Hide hotel_url, id, longitude, latitude, currentUser columns
        TableColumnModel tcm = table.getColumnModel();
        table.getColumnModel().removeColumn(tcm.getColumn(15));
        table.getColumnModel().removeColumn(tcm.getColumn(14));
        table.getColumnModel().removeColumn(tcm.getColumn(13));
        table.getColumnModel().removeColumn(tcm.getColumn(12));
        table.getColumnModel().removeColumn(tcm.getColumn(11));
        if(!showDistance)
            table.getColumnModel().removeColumn(tcm.getColumn(10));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.setDefaultRenderer(Double.class, centerRenderer);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.getColumn("Hotel").setCellRenderer(centerRenderer);
        URLRenderer urlRenderer = new URLRenderer();
        urlRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumn("Comments").setCellRenderer(urlRenderer);
        table.getColumn("POIs").setCellRenderer(urlRenderer);

        /*MultiLineTableCellRenderer multiRenderer = new MultiLineTableCellRenderer();
        table.setDefaultRenderer(String[].class, multiRenderer);
        */

        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++)
            {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            table.setRowHeight(row, rowHeight);
        }

        resizeColumnWidth(table);

        Color color = UIManager.getColor("Table.gridColor");
        MatteBorder border = new MatteBorder(2, 2, 2, 2, color);
        table.setBorder(border);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addMouseListener(new MouseAdapter() {
                                   public void mousePressed(MouseEvent me) {
                                       JTable tab =(JTable) me.getSource();
                                       Point p = me.getPoint();
                                       int row = tab.rowAtPoint(p);
                                       int column = tab.columnAtPoint(p);
                                       if (column==3){ //Reviews
                                           tab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                           int numReviews = (Integer) tab.getModel().getValueAt(row,column);
                                           String hotelName = (String) tab.getModel().getValueAt(row,0);
                                           Integer hotelId = (Integer) tab.getModel().getValueAt(row,12);
                                           Document hotelFilter = new Document();
                                           hotelFilter.append("id",hotelId);
                                           MongoDatabase db = DbUtils.getDatabase();
                                           MongoCollection<Document> collectionHotels = db.getCollection("hotels");
                                           FindIterable<Document> doc = collectionHotels.find(hotelFilter);

                                           if (doc!=null && doc.iterator().hasNext()) {

                                               Document hotel = doc.iterator().next();
                                               ArrayList<Document> hotelReviews = (ArrayList) hotel.get("reviews");
                                               if (hotelReviews!=null) {
                                                   showComments(numReviews, hotelReviews, hotelName);
                                                   tab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                               }else{
                                                   JOptionPane.showMessageDialog(frame,"There aren't any reviews for this hotel");
                                                   tab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                               }
                                           }
                                       }else if (column==9){ // Show POIS
                                           tab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                           MongoDatabase db = DbUtils.getDatabase();
                                           MongoCollection<Document> collectionPois = db.getCollection("pois");
                                           MongoCollection<Document> collectionUsers = db.getCollection("users");

                                           String currentUser = (String) tab.getModel().getValueAt(row,15);

                                           FindIterable<Document> userIt = QueryHelper.getUser(collectionUsers, currentUser);
                                           Iterator userIterator = userIt.iterator();
                                           double minDistance = 0d;
                                           double maxDistance = 1000d;
                                           int userPoisLimit = 10;
                                           String[] poisTypes = null;
                                           if (userIterator.hasNext()) {
                                               Document user = (Document) userIterator.next();
                                               minDistance = user.getDouble("poisMinDistance");
                                               maxDistance = user.getDouble("poisMaxDistance");
                                               userPoisLimit = user.getInteger("poisNumberLimit");
                                               poisTypes = user.getString("poisTypes").split(",");
                                           }

                                           ArrayList<Object> valuesFilter = new ArrayList();

                                           if (poisTypes.length==1 && poisTypes[0].equals("0")){
                                               //All POIs
                                               Iterator it = PoisTypes.poisTypes.values().iterator();
                                               while(it.hasNext()){
                                                   valuesFilter.add(it.next());
                                               }

                                           }else {
                                               for (int j = 0; j < poisTypes.length; j++) {
                                                   String poiType = PoisTypes.poisTypes.get(new Integer(poisTypes[j]));
                                                   valuesFilter.add(poiType);
                                               }
                                           }

                                           double longitude = (Double) tab.getModel().getValueAt(row,13);
                                           double latitude = (Double) tab.getModel().getValueAt(row,14);

                    /*
                    String keyFilter = "type";
                    String filterOperatorType = "or";
                    MongoCursor<Document> pois = QueryHelper.findByDistanceAndFilters(collectionPois,longitude,latitude,minDistance, maxDistance,keyFilter,valuesFilter,filterOperatorType);
                    int numPois=0;
                    ArrayList<Document> poiList = new ArrayList();
                    while (pois.hasNext() && numPois<userPoisLimit) {
                        Document poi = pois.next();
                        numPois++;
                        poiList.add(poi);
                    }*/

                                           Document pois = QueryHelper.findByDistance("pois", longitude,latitude,minDistance,maxDistance);

                                           ArrayList results = (ArrayList) pois.get("results");

                                           int numPois=0;
                                           ArrayList<Document> poiList = new ArrayList();

                                           for(int i =0;i<results.size();i++){
                                               Double distance = ((Document) results.get(i)).getDouble("dis");
                                               distance = distance * 6371 * 1000; //Transform radians (returned by $geoNear) to meters
                                               Document poi= (Document) ((Document) results.get(i)).get("obj");
                                               String poiType = poi.getString("poi_type");
                                               if(valuesFilter.contains(poiType)) {
                                                   numPois++;
                                                   poi.append("distance",distance);
                                                   poiList.add(poi);
                                               }
                                               if(numPois==userPoisLimit)
                                                   break;
                                           }

                                           String hotelName = (String) tab.getModel().getValueAt(row,0);

                                           if (numPois>0) {
                                               ui.showPois(numPois, poiList, hotelName);
                                               tab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                           }else{
                                               JOptionPane.showMessageDialog(frame,"There aren't POIs that fulfill user settings");
                                               tab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                           }

                                       }
                                       else if (me.getClickCount() == 2) {
                                           String hotel_url = (String) tab.getModel().getValueAt(row,11);
                                           if (Desktop.isDesktopSupported()) {
                                               try {
                                                   Desktop.getDesktop().browse(new URI(hotel_url));
                                               } catch (Exception e) {
                                               }
                                           }
                                       }
                                   }
                               }
        );

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(2000, 2000));

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        JScrollPane jScrollPane = new JScrollPane(table);
        container.add(jScrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    private void showComments(int numRows,ArrayList<Document> reviewList, String hotelName){
        Object [][] data  = new Object[numRows][];
        String columnNames[] = {"Date", "User", "Age", "Location", "Cleanliness", "Confort", "Installations", "Personal", "WIFI", "Value for Money", "Total Score", "Comment"};
        int i = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        TimeZone zone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(zone);

        if (reviewList!=null) {

            for (Document review : reviewList) {
                if (i == numRows)
                    break;

                Object[] row = new Object[columnNames.length];

                Date date = new Date(review.getLong("date"));
                row[0] = sdf.format(date);
                row[1] = review.getString("username");
                row[2] = (review.getInteger("age")!=null)?review.getInteger("age").toString(): "-";
                row[3] = review.getDouble("location");
                row[4] = review.getDouble("cleanliness");
                row[5] = review.getDouble("confort");
                row[6] = review.getDouble("installations");
                row[7] = review.getDouble("personal");
                row[8] = review.getDouble("wifi");
                row[9] = review.getDouble("valueMoney");
                row[10] = review.getDouble("score");
                row[11] = review.getString("comment");

                data[i] = row;
                i++;
            }

            //TextTable textTable = new TextTable(columnNames, data);
            //textTable.printTable();

            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public Class getColumnClass(int column) {
                    return getValueAt(0, column).getClass();
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JFrame frame = new JFrame();
            frame.setTitle("Hotel Advisor - Comments about hotel: " + hotelName);

            for (int j = 0; j < columnNames.length; j++) {
                tableModel.addColumn(columnNames[j]);
            }

            for (int j = 0; j < data.length; j++) {
                tableModel.addRow(data[j]);
            }

            JTable table = new JTable(tableModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
                                                 int columnIndex) {
                    JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                    if (columnIndex >= 3 && columnIndex <= 10) {
                        double value = (Double) getValueAt(rowIndex, columnIndex);
                        if (value <= 5)
                            component.setBackground(new Color(255, 8, 25));
                        else if (value < 7)
                            component.setBackground(new Color(255, 185, 23));
                        else if (value < 8)
                            component.setBackground(new Color(180, 255, 20));
                        else if (value < 9)
                            component.setBackground(new Color(34, 255, 14));
                        else
                            component.setBackground(new Color(8, 255, 140));
                    } else
                        component.setBackground(new Color(255, 255, 255));

                    return component;
                }
            };

            for (int row = 0; row < table.getRowCount(); row++) {
                int rowHeight = table.getRowHeight();

                for (int column = 0; column < table.getColumnCount(); column++) {
                    Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                    rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
                }
                table.setRowHeight(row, rowHeight);
            }

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            //table.setDefaultRenderer(String.class, centerRenderer);
            table.setDefaultRenderer(Double.class, centerRenderer);
            table.getColumn("User").setCellRenderer(centerRenderer);
            table.getColumn("Age").setCellRenderer(centerRenderer);

            resizeColumnWidth(table);

            Color color = UIManager.getColor("Table.gridColor");
            MatteBorder border = new MatteBorder(2, 2, 2, 2, color);
            table.setBorder(border);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setPreferredSize(new Dimension(2000, 2000));

            Container container = frame.getContentPane();
            container.setLayout(new BorderLayout());
            JScrollPane jScrollPane = new JScrollPane(table);
            container.add(jScrollPane);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public void showPois(int numRows,ArrayList<Document> poiList, String hotelName){
        Object [][] data  = new Object[numRows][];
        String columnNames[] = {"POI", "Image", "Type", "Coord", "Distance (m)"};
        int i = 0;

        for (Document poi: poiList){
            if (i==numRows)
                break;

            Object[] row = new Object[columnNames.length];

            row[0] = poi.getString("poi_name");

            String poiType = poi.getString("poi_type");

            final String dir = System.getProperty("user.dir");
            String imagePath;
            if (dir.contains("target"))
                imagePath = "../src/main/resources/" +  poiType + ".PNG";
            else
                imagePath = "./src/main/resources/" +  poiType + ".PNG";

            ImageIcon imageIcon = null;
            try {
                 imageIcon = new ImageIcon(imagePath);

            }catch(Exception e){
                if (dir.contains("target"))
                    imagePath = "../src/main/resources/not_available.png";
                else
                    imagePath = "./src/main/resources/not_available.png";

                imageIcon = new ImageIcon(imagePath);
            }

            row[1] = imageIcon;

            row[2] = poiType;

            ArrayList<Double> coords = (ArrayList) poi.get("poi_coord");
            double longitude = coords.get(0);
            double latitude = coords.get(1);

            row[3] = "(" + longitude + "," + latitude + ")";

            row[4] = Maths.round(poi.getDouble("distance"),2);

            data[i] = row;
            i++;
        }

        //TextTable textTable = new TextTable(columnNames, data);
        //textTable.printTable();

        DefaultTableModel tableModel = new DefaultTableModel()
        {
            @Override
            public Class getColumnClass(int column)
            {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JFrame frame = new JFrame();
        frame.setTitle("Hotel Advisor - POIS near hotel: " + hotelName);

        for (int j=0;j<columnNames.length;j++) {
            tableModel.addColumn(columnNames[j]);
        }

        for (int j=0;j<data.length;j++) {
            tableModel.addRow(data[j]);
        }

        JTable table = new JTable(tableModel);

        for (int row = 0; row < table.getRowCount(); row++)
        {
            int rowHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++)
            {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            table.setRowHeight(row, rowHeight);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.setDefaultRenderer(String.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);

        resizeColumnWidth(table);

        Color color = UIManager.getColor("Table.gridColor");
        MatteBorder border = new MatteBorder(2, 2, 2, 2, color);
        table.setBorder(border);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(2000, 2000));

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        JScrollPane jScrollPane = new JScrollPane(table);
        container.add(jScrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    public void showPersonalHotelReviews(int numRows,ArrayList<Document> reviewList, MongoCollection<Document> collectionHotels, MongoCollection<Document> collectionUsers, String currentUser){
        Object [][] data  = new Object[numRows][];
        String columnNames[] = {"Date", "Hotel", "Image", "Score", "Category(*)", "Price(EUR)", "Rooms", "City", "Comment", "Location", "Cleanliness", "Confort", "Installations", "Personal", "WIFI", "Value for Money", "Total Score", "Hotel URL", "Hotel Id", "Current User"};
        int i = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        TimeZone zone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(zone);

        for (Document review: reviewList) {

            Object[] row = new Object[columnNames.length];

            int hotelId = review.getInteger("hotel_id");

            Document hotelFilter = new Document();
            hotelFilter.append("id",hotelId);

            FindIterable<Document> doc = collectionHotels.find(hotelFilter);

            if (doc!=null && doc.iterator().hasNext()) {

                Document hotel = doc.iterator().next();
                Date date = new Date(review.getLong("date"));
                row[0] = sdf.format(date);

                row[1] = hotel.getString("name");
                ImageIcon imageIcon = null;
                try {
                    URL url = new URL(hotel.getString("photo_url"));
                    Image img = ImageIO.read(url);
                    Image resizedImage = img.getScaledInstance(250, 250, 0);
                    imageIcon = new ImageIcon(resizedImage);
                }catch (Exception e){
                    final String dir = System.getProperty("user.dir");
                    String imagePath;
                    if (dir.contains("target"))
                        imagePath = "../src/main/resources/not_available.png";
                    else
                        imagePath = "./src/main/resources/not_available.png";
                    imageIcon = new ImageIcon(imagePath);
                }

                row[2] = imageIcon;
                row[3] = Maths.round(hotel.getDouble("review_score"),2);
                row[4] = hotel.getDouble("class");
                Double min_rate_EUR = hotel.getDouble("min_rate_EUR");
                if (min_rate_EUR!=null)
                    min_rate_EUR = Maths.round(min_rate_EUR,2);
                else
                    min_rate_EUR =0.0d;
                row[5] = min_rate_EUR;
                row[6]= hotel.getInteger("nr_rooms");
                row[7]=hotel.getString("city_hotel");
                row[8]= review.getString("comment");

                row[9] = review.getDouble("location");
                row[10] = review.getDouble("cleanliness");
                row[11] = review.getDouble("confort");
                row[12] = review.getDouble("installations");
                row[13] = review.getDouble("personal");
                row[14] = review.getDouble("wifi");
                row[15] = review.getDouble("valueMoney");
                row[16] = review.getDouble("score");
                row[17] = hotel.getString("hotel_url");
                row[18] = hotel.getInteger("id");
                row[19] = currentUser;

                data[i] = row;

                i++;

            }
        }

        //TextTable textTable = new TextTable(columnNames, data);
        //textTable.printTable();

        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public Class getColumnClass(int column)
            {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        final JFrame frame = new JFrame();
        frame.setTitle("Hotel Advisor - Hotels reviewed by user: " + currentUser);

        for (int j=0;j<columnNames.length;j++) {
            tableModel.addColumn(columnNames[j]);
        }

        for (int j=0;j<data.length;j++) {
            tableModel.addRow(data[j]);
        }

        JTable table = new JTable(tableModel){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
                                             int columnIndex) {
                JComponent component = (JComponent) super.prepareRenderer(renderer, rowIndex, columnIndex);

                if(columnIndex == 3 || (columnIndex >= 9 && columnIndex <= 16) ) {
                    double value = (Double) getValueAt(rowIndex,columnIndex);

                    if (value<=5)
                        component.setBackground(new Color(255, 8, 25));
                    else if(value<7)
                        component.setBackground(new Color(255, 185, 23));
                    else if(value<8)
                        component.setBackground(new Color(180, 255, 20));
                    else if(value<9)
                        component.setBackground(new Color(34, 255, 14));
                    else
                        component.setBackground(new Color(8, 255, 140));
                }
                else
                    component.setBackground(new Color (255,255,255));

                return component;
            }
        };

        //Hide hotel_url, id, currentUser columns
        TableColumnModel tcm = table.getColumnModel();
        table.getColumnModel().removeColumn(tcm.getColumn(19));
        table.getColumnModel().removeColumn(tcm.getColumn(18));
        table.getColumnModel().removeColumn(tcm.getColumn(17));


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.setDefaultRenderer(Double.class, centerRenderer);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.getColumn("Hotel").setCellRenderer(centerRenderer);

        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++)
            {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            table.setRowHeight(row, rowHeight);
        }

        resizeColumnWidth(table);

        Color color = UIManager.getColor("Table.gridColor");
        MatteBorder border = new MatteBorder(2, 2, 2, 2, color);
        table.setBorder(border);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addMouseListener(new MouseAdapter() {
                                   public void mousePressed(MouseEvent me) {
                                       JTable tab =(JTable) me.getSource();
                                       Point p = me.getPoint();
                                       int row = tab.rowAtPoint(p);
                                       if (me.getClickCount() == 2) {
                                           String hotel_url = (String) tab.getModel().getValueAt(row,17);
                                           if (Desktop.isDesktopSupported()) {
                                               try {
                                                   Desktop.getDesktop().browse(new URI(hotel_url));
                                               } catch (Exception e) {
                                               }
                                           }
                                       }
                                   }
                               }
        );

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(2000, 2000));

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        JScrollPane jScrollPane = new JScrollPane(table);
        container.add(jScrollPane);
        frame.pack();
        frame.setVisible(true);
    }


    public void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 125; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            int preferredWidth = columnModel.getColumn(column).getPreferredWidth();
            width = Math.max(preferredWidth, width);
            columnModel.getColumn(column).setPreferredWidth(width + 1);
        }
    }

    public class MultiLineTableCellRenderer extends JList<String> implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof String[]) {
                setListData((String[]) value);
            }

            if (isSelected) {
                setBackground(UIManager.getColor("Table.selectionBackground"));
                setForeground(new Color(0, 0, 0));
            } else {
                setBackground(UIManager.getColor("Table.background"));
                setForeground(new Color(0, 0, 0));
            }

            return this;
        }
    }

    class URLRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
        private int row = -1;
        private int col = -1;
        private boolean isRollover = false;

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            this.isRollover = isURLColumn(column);

            if (this.isRollover)
                setText("<html> <u> <font color='blue'>" + value.toString());

            return this;
        }

        private boolean isURLColumn(int column) {
            return column == 3 || column==9;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            JTable table = (JTable) e.getSource();
            Point pt = e.getPoint();
            int prev_row = row;
            int prev_col = col;
            boolean prev_ro = isRollover;
            row = table.rowAtPoint(pt);
            col = table.columnAtPoint(pt);
            isRollover = isURLColumn(col);
            if ((row == prev_row && col == prev_col && Boolean.valueOf(isRollover).equals(prev_ro)) ||
                    (!isRollover && !prev_ro)) {
                return;
            }

            Rectangle repaintRect;
            if (isRollover) {
                Rectangle r = table.getCellRect(row, col, false);
                repaintRect = prev_ro ? r.union(table.getCellRect(prev_row, prev_col, false)) : r;
            } else {
                repaintRect = table.getCellRect(prev_row, prev_col, false);
            }
            table.repaint(repaintRect);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JTable table = (JTable) e.getSource();
            if (isURLColumn(col)) {
                table.repaint(table.getCellRect(row, col, false));
                row = -1;
                col = -1;
                isRollover = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            JTable table = (JTable) e.getSource();
            Point pt = e.getPoint();
            int row = table.rowAtPoint(pt);
            int column = table.columnAtPoint(pt);
            if (column == 3) {
                int numReviews = (Integer) table.getModel().getValueAt(row, column);
                String hotelName = (String) table.getModel().getValueAt(row, 0);
                Integer hotelId = (Integer) table.getModel().getValueAt(row, 11);
                Document hotelFilter = new Document();
                hotelFilter.append("id", hotelId);
                MongoDatabase db = DbUtils.getDatabase();
                MongoCollection<Document> collectionHotels = db.getCollection("hotels");
                FindIterable<Document> doc = collectionHotels.find(hotelFilter);

                if (doc != null && doc.iterator().hasNext()) {

                    Document hotel = doc.iterator().next();
                    ArrayList<Document> hotelReviews = (ArrayList) hotel.get("reviews");
                    if (hotelReviews != null)
                        showComments(numReviews, hotelReviews, hotelName);
                }
            }
        }
    }

}
