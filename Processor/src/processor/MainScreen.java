package processor;

import java.util.List;
import javax.swing.table.DefaultTableModel;

public class MainScreen extends javax.swing.JFrame {
    
    private Processor processor;
    
    public MainScreen() {
        initComponents();
    }

    private void updateTable(){
        List<RegisterStatus> registerStatus = processor.getRegisterStatus();
        List<ReorderBuffer> reorderBuffer = processor.getRo();
        List<ReservationStation> reservation = processor.getReservationStations();
        
        DefaultTableModel regTable = (DefaultTableModel)registerTable.getModel();
        DefaultTableModel roTable = (DefaultTableModel)ReorderBufferTable.getModel();
        DefaultTableModel reserveTable = (DefaultTableModel)ReservationTable.getModel();
        int count = 0;
        
        for(RegisterStatus rs:registerStatus){
            String qi = "";
            String status = String.valueOf(rs.status);
            if (rs.status){
                qi = "ER"+String.valueOf(rs.qi+1);
            }
            regTable.setValueAt(qi, count%8+1, 1+3*(count/8));
            regTable.setValueAt(status, count%8+1, 2+3*(count/8));
            count++;
        }
        
        for (ReorderBuffer ro:reorderBuffer){
            
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ReservationTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        ReorderBufferTable = new javax.swing.JTable();
        label1 = new java.awt.Label();
        label2 = new java.awt.Label();
        nextButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        registerTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ReservationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"ER1", "Load/Store", null, null, null, null, null, null, null},
                {"ER2", "Load/Store", null, null, null, null, null, null, null},
                {"ER3", "Load/Store", null, null, null, null, null, null, null},
                {"ER4", "Load/Store", null, null, null, null, null, null, null},
                {"ER5", "Load/Store", null, null, null, null, null, null, null},
                {"ER6", "Add", null, null, null, null, null, null, null},
                {"ER7", "Add", null, null, null, null, null, null, null},
                {"ER8", "Add", null, null, null, null, null, null, null},
                {"ER9", "Mult", null, null, null, null, null, null, null},
                {"ER10", "Mult", null, null, null, null, null, null, null},
                {"ER11", "Mult", null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Tipo", "Instrução", "Dest.", "Vj", "Vk", "Qj", "Qk", "A"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(ReservationTable);

        ReorderBufferTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Entrada", "Ocupado", "Instrução", "Estado", "Destino", "Valor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(ReorderBufferTable);

        label1.setText("Estações de reserva");

        label2.setText("Buffer de reordenação");

        nextButton.setText("Próximo");
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        startButton.setText("Iniciar");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Registradores");

        registerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"R0", null, null, "R8", null, null, "R16", null, null, "R24", null, null},
                {"R1", null, null, "R9", null, null, "R17", null, null, "R25", null, null},
                {"R2", null, null, "R10", null, null, "R18", "", null, "R26", null, null},
                {"R3", null, null, "R11", null, null, "R19", null, null, "R27", null, null},
                {"R4", null, null, "R12", null, null, "R20", null, null, "R28", null, null},
                {"R5", null, null, "R13", null, null, "R21", null, null, "R29", null, null},
                {"R6", null, null, "R14", null, null, "R22", null, null, "R30", null, null},
                {"R7", null, null, "R15", null, null, "R23", null, null, "R31", null, null}
            },
            new String [] {
                "", "Qi", "Vi", "", "Qi", "Vi", "", "Qi", "Vi", "", "Qi", "Vi"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(registerTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nextButton))
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 653, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startButton)
                            .addComponent(nextButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1))
                    .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 411, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        processor = new Processor();
        if (processor==null)return;
        startButton.setEnabled(false);
        nextButton.setEnabled(true);
        updateTable();
    }//GEN-LAST:event_startButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        processor.nextClock();
        updateTable();
    }//GEN-LAST:event_nextButtonActionPerformed

    
    public static void main(String args[]) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        

        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainScreen().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable ReorderBufferTable;
    private javax.swing.JTable ReservationTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private javax.swing.JButton nextButton;
    private javax.swing.JTable registerTable;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
