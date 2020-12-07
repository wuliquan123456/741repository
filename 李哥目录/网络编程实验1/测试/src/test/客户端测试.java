package test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;


public class �ͻ��˲��� {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    private JScrollPane scrollPane = null;
    private JTextPane text = null;
    private JTextField addText = null; // ���������
    private StyledDocument doc = null;
    public �ͻ��˲���(String serverAddress) {
        this.serverAddress = serverAddress;

        text = new JTextPane();//��Ϣ��ʾ��
        text.setEditable(false);//����Ϣ��ʾ������Ϊ�����Ա༭
        doc = text.getStyledDocument(); // ���JTextPane��Document
        scrollPane = new JScrollPane(text);//����Ϣ��ʾ������Ϊ�ɻ�������
        scrollPane.setPreferredSize(new Dimension(500, 500));
        addText = new JTextField(10);//�ı�������
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(addText, BorderLayout.SOUTH);
        frame.pack();

        //�ı���������Ӧ�¼�
        addText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(addText.getText());//ȡ�����������ݲ����͵���������
                addText.setText("");//���ı������
            }
        });
    }
    private String getName() {
        return JOptionPane.showInputDialog(frame, "", "�������û�����",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
        	Socket socket = new Socket(serverAddress, 59001);//ͨ��ip��ַ�Ͷ˿ں������Ϸ�����
            in = new Scanner(socket.getInputStream());//������
            out = new PrintWriter(socket.getOutputStream(), true);//�����

            while (in.hasNextLine()) {//�յ���Ϣ
                String line = in.nextLine();//���յ�����Ϣ��ֵ���ַ���line
                if (line.startsWith("SUBMITNAME")) {//�������˷�����������������
                    out.println(getName());  //���������������ֲ����ط�������
                } else if (line.startsWith("NAMEACCEPTED")) {//�������˷����������Ѿ��յ�
                    this.frame.setTitle("�û��� " + line.substring(13));//���������˷������������óɴ�������
                    addText.setEditable(true);//����������óɿɱ༭
                } else if (line.startsWith("MESSAGE")) {//����˷�����Ϣ
                	//������ʽƥ�䣬 #10xx# --����������#�ż�10��ͷ����λ���� ��ʶ��ΪͼƬ����
                    String str=line;
                    if(str.matches(".*#10[0-9]{2}.*")){//�Ƿ����ͼƬ���ţ��������ƥ��
                        String strs[]=str.split("#");
                        for(int i=0;i<strs.length;i++){
                            if(i==0){//��һ���ַ�������ض�������MESSAGE+name�����ض�����ͼƬ����
                                try {
                                    doc.insertString(doc.getLength(), strs[i].substring(8) + " ",null);//ȥ����MESSAGE�����ӵ�8���ַ���ʼȡֵ
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                if(strs[i].matches("10[0-9]{2}")){//ֻ��4����������10��ͷ
                                    String path="src/images/"+strs[i]+".gif";//ͼƬ·��
                                    try{
                                    text.setCaretPosition(doc.getLength()-1);//ͼƬ����λ�ã��ı����
                                    text.insertIcon(new ImageIcon(path)); // ����ͼƬ
                                    if(i==strs.length-1){//�����������һ���ַ��������ˣ�����л���
                                    	doc.insertString(doc.getLength()-1,  "\n\n",null);//���������հ���
                                    }
                                    }catch(Exception e){
                                    	
                                    }

                                }else{//������4������
                                    try {
                                        doc.insertString(doc.getLength()-1, strs[i] + " ",null);
                                        if(i==strs.length-1){//�����������һ���ַ��������ˣ�����л���
                                        	doc.insertString(doc.getLength()-1,  "\n\n",null);
                                        }
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            

                        }
                    }else {//������ͼƬ���ţ����������
                        try {
                            doc.insertString(doc.getLength(), line.substring(8) + "\n\n",null);//�ӵڰ˸��ַ������ֿ�ʼȡֵ
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                	
 
                    
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
    	�ͻ��˲��� client = new �ͻ��˲���("172.18.42.99");//��������ip��ַ
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}