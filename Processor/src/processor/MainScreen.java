package processor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class MainScreen extends javax.swing.JFrame {
    
    private Processor processor;
    
    public MainScreen() {
        initComponents();
    }

    private void updateTable(){
        List<Register> registerStatus = processor.getR();
        List<ReorderBuffer> reorderBuffer = processor.getRob();
        List<ReservationStation> reservationStation = new ArrayList<>();
        reservationStation.addAll(processor.getReservationStationsMemoria());
        reservationStation.addAll(processor.getReservationStationsSoma());
        reservationStation.addAll(processor.getReservationStationsMultiplicacao());
        
        DefaultTableModel regTable = (DefaultTableModel)registerTable.getModel();
        DefaultTableModel roTable = (DefaultTableModel)ReorderBufferTable.getModel();
        DefaultTableModel reserveTable = (DefaultTableModel)ReservationTable.getModel();
        int count = 0;
        
        for(Register rs: registerStatus){
            String qi = "";
            String status = rs.busy?"1":"0";
            if (rs.busy){
                qi = "ER"+String.valueOf(rs.qi+1);
            }
            regTable.setValueAt(qi, count%8, 1+3*(count/8));
            regTable.setValueAt(status, count%8, 2+3*(count/8));
            count++;
        }
        
        count = 1;
        reserveTable.setRowCount(0);
        for (ReservationStation rs: reservationStation){
            String id = "ER"+count++;
            String type = rs.name;
            String busy = rs.busy?"Sim":"Nao";
            String instruction = rs.op == Operation.EMPTY?"":rs.op.toString();
            String destination = rs.dest==-1?"":String.valueOf(rs.dest);
            String vj = rs.vj==-1?"":String.valueOf(rs.vj);
            String vk = rs.vk==-1?"":String.valueOf(rs.vk);
            String qj = rs.qj==-1?"":String.valueOf(rs.qj);
            String qk = rs.qk==-1?"":String.valueOf(rs.qk);
            String A = rs.A==-1?"":String.valueOf(rs.A);
            
            reserveTable.addRow(new String[]{
                id,
                type,
                busy,
                instruction,
                destination,
                vj,
                vk,
                qj,
                qk,
                A
            });
        }
        
        for (ReorderBuffer ro:reorderBuffer){
            
        }
        
        int clock = processor.getClock();
        int instruction = processor.getInstructionCounter();
        int pc = processor.getPc();
        
        clockLabel.setText(String.valueOf(clock));
        pcLabel.setText(String.valueOf(pc));
        instructionsLabel.setText(String.valueOf(instruction));
        cpiLabel.setText(String.valueOf((double)clock/instruction));
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        instructionsLabel = new javax.swing.JLabel();
        cpiLabel = new javax.swing.JLabel();
        pcLabel = new javax.swing.JLabel();
        clockLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ReservationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"ER1", "Load/Store", null, null, null, null, null, null, null, null},
                {"ER2", "Load/Store", null, null, null, null, null, null, null, null},
                {"ER3", "Load/Store", null, null, null, null, null, null, null, null},
                {"ER4", "Load/Store", null, null, null, null, null, null, null, null},
                {"ER5", "Load/Store", null, null, null, null, null, null, null, null},
                {"ER6", "Add", null, null, null, null, null, null, null, null},
                {"ER7", "Add", null, null, null, null, null, null, null, null},
                {"ER8", "Add", null, null, null, null, null, null, null, null},
                {"ER9", "Mult", null, null, null, null, null, null, null, null},
                {"ER10", "Mult", null, null, null, null, null, null, null, null},
                {"ER11", "Mult", null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Tipo", "Busy", "Instrução", "Dest.", "Vj", "Vk", "Qj", "Qk", "A"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
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

        jLabel2.setText("Clock corrente:");

        jLabel3.setText("PC:");

        jLabel4.setText("Instruções concluídas:");

        jLabel5.setText("CPI:");

        instructionsLabel.setText("?");

        cpiLabel.setText("?");

        pcLabel.setText("?");

        clockLabel.setText("?");

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
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel5)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(0, 0, 0)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(instructionsLabel)
                                    .addComponent(pcLabel)
                                    .addComponent(clockLabel)
                                    .addComponent(cpiLabel)))
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(clockLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pcLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(instructionsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cpiLabel)))
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
        clockLabel.setText("0");
        pcLabel.setText("0");
        instructionsLabel.setText("0");
        cpiLabel.setText("0");
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
    private javax.swing.JLabel clockLabel;
    private javax.swing.JLabel cpiLabel;
    private javax.swing.JLabel instructionsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel pcLabel;
    private javax.swing.JTable registerTable;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
