package processor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class MainScreen extends javax.swing.JFrame {
    
    private Processor processor;
    private boolean execute;
    
    public MainScreen() {
        initComponents();
        execute = false;
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
            String qi = rs.busy?String.valueOf(rs.qi.id):"";
            String vi = rs.value==-1?"?":String.valueOf(rs.value);
            regTable.setValueAt(qi, count%8, 1+3*(count/8));
            regTable.setValueAt(vi, count%8, 2+3*(count/8));
            count++;
        }
        
        int clock = processor.getClock();
        
        count = 1;
        reserveTable.setRowCount(0);
        for (ReservationStation rs: reservationStation){
            String id = "ER"+count++;
            String type = rs.name;
            String busy = rs.isBusy(clock)?"Sim":"Nao";
            String instruction = rs.instruction;
            String destination = rs.reorder==null?"":"#"+String.valueOf(rs.id);
            String vj = rs.vj==-1?"":String.valueOf(rs.vj);
            String vk = rs.vk==-1?"":String.valueOf(rs.vk);
            String qj = rs.qj==null?"":"#"+String.valueOf(rs.qj.id);
            String qk = rs.qk==null?"":"#"+String.valueOf(rs.qk.id);
            String A = rs.A==-1?"":String.valueOf(rs.A);
            
            reserveTable.addRow(new String[]{
                id, type, busy, instruction, destination, vj, vk, qj, qk, A
            });
        }
        
        count = 1;
        roTable.setRowCount(0);
        for (ReorderBuffer rob : reorderBuffer) {
            if (rob.state == State.EMPTY) {
                continue;
            }
            String input = String.valueOf(count++);
            String busy = rob.isBusy(clock) ? "sim" : "nao";
            String instruction = rob.co.instruction;
            String state = String.valueOf(rob.state);
            String destination = rob.destination == null ? "" : "Reg[" + rob.destination.id + "]";
            
            ReservationStation res = rob.station;
            if (res.op == Operation.SW){
                destination = "Mem[" +res.A+ " + R[" + rob.co.rs + "]]";
            }
            
            String value = rob.value == -1 ? "" : String.valueOf(rob.value);
            
            roTable.addRow(new String[]{
                input, busy, instruction, state, destination, value
            });
        }
        
        int instruction = processor.getInstructionCounter();
        int pc = processor.getPc();
        
        NumberFormat formatter = new DecimalFormat("#0.000");
        String CPI = instruction==0?"0":String.valueOf(formatter.format((double)clock/instruction));
        
        clockLabel.setText(String.valueOf(clock));
        pcLabel.setText(String.valueOf(pc));
        instructionsLabel.setText(String.valueOf(instruction));
        cpiLabel.setText(CPI);
    }
    
    void clearTables(){
        DefaultTableModel regTable = (DefaultTableModel)registerTable.getModel();
        DefaultTableModel roTable = (DefaultTableModel)ReorderBufferTable.getModel();
        DefaultTableModel reserveTable = (DefaultTableModel)ReservationTable.getModel();
        
        roTable.setRowCount(0);
        
        for(int i=0;i<32;i++){
            regTable.setValueAt("", i%8, 1+3*(i/8));
            regTable.setValueAt("", i%8, 2+3*(i/8));
        }
        reserveTable.setNumRows(0);
        
        for (int i=0;i<5;i++){
            reserveTable.addRow(new String[]{"ER"+String.valueOf(i+1),"Load/Store"});
        }for (int i=5;i<8;i++){
            reserveTable.addRow(new String[]{"ER"+String.valueOf(i+1),"Add"});
        }for (int i=8;i<10;i++){
            reserveTable.addRow(new String[]{"ER"+String.valueOf(i+1),"Mult"});
        }
        
        clockLabel.setText("?");
        cpiLabel.setText("?");
        pcLabel.setText("?");
        instructionsLabel.setText("?");
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
        clearButton = new javax.swing.JToggleButton();
        autoButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

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
                {"ER10", "Mult", null, null, null, null, null, null, null, null}
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

        clearButton.setText("Clear");
        clearButton.setEnabled(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        autoButton.setText("Exec. auto");
        autoButton.setEnabled(false);
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        pauseButton.setText("Parar");
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(37, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 653, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(startButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(nextButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(clearButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(autoButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(pauseButton))
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(instructionsLabel)
                                    .addComponent(pcLabel)
                                    .addComponent(clockLabel)
                                    .addComponent(cpiLabel))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                            .addComponent(nextButton)
                            .addComponent(clearButton)
                            .addComponent(autoButton)
                            .addComponent(pauseButton))
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
        clearButton.setEnabled(true);
        autoButton.setEnabled(true);
        clockLabel.setText("0");
        pcLabel.setText("0");
        instructionsLabel.setText("0");
        cpiLabel.setText("0");
    }//GEN-LAST:event_startButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        processor.nextClock();
        updateTable();
    }//GEN-LAST:event_nextButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearTables();
        startButton.setEnabled(true);
        nextButton.setEnabled(false);
        clearButton.setEnabled(false);
        autoButton.setEnabled(false);
    }//GEN-LAST:event_clearButtonActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
        nextButton.setEnabled(false);
        clearButton.setEnabled(false);
        autoButton.setEnabled(false);
        pauseButton.setEnabled(true);
        
        execute = true;
        for(int c=0; c<500; c++){
            processor.nextClock();
        }
        updateTable();
        startButton.setEnabled(true);
    }//GEN-LAST:event_autoButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        pauseButton.setEnabled(false);
        execute = false;
    }//GEN-LAST:event_pauseButtonActionPerformed

    
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
    private javax.swing.JButton autoButton;
    private javax.swing.JToggleButton clearButton;
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
    private javax.swing.JButton pauseButton;
    private javax.swing.JLabel pcLabel;
    private javax.swing.JTable registerTable;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
