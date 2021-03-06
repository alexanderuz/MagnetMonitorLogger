import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

public class TrayFrame extends JFrame {
    public static TrayFrame app;
    private TrayIcon trayIcon;
    private SystemTray systemTray = SystemTray.getSystemTray();
    public boolean chetTray = false; //переменная, чтобы был вывод сообщения в трее только при первом сворачивании

    public TrayFrame() throws IOException {
        super("Демонстрация сворачивания в трей");
        URL imageURL = this.getClass().getResource("/Ikonka.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(imageURL);
        trayIcon = new TrayIcon(icon, "Демонстрация сворачивания в трей"); //Ikonka.png - изображение, которое будет показываться в трее - картинка в каталоге исполняемого приложения
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setVisible(true);
                setState(JFrame.NORMAL);
                removeTray();
            }
        });
        //обработчик мыши
        MouseListener mouS = new MouseListener() {
            public void mouseClicked(MouseEvent ev) {
                setVisible(true);
                setState(JFrame.NORMAL);
                removeTray();
            }

            public void mouseEntered(MouseEvent ev) {
            }

            public void mouseExited(MouseEvent ev) {
            }

            public void mousePressed(MouseEvent ev) {
            }

            public void mouseReleased(MouseEvent ev) {
            }
        };
        trayIcon.addMouseListener(mouS);
        MouseMotionListener mouM = new MouseMotionListener() {
            public void mouseDragged(MouseEvent ev) {
            }

            //при наведении
            public void mouseMoved(MouseEvent ev) {
                boolean flg = false;
                trayIcon.setToolTip("Двойной щелчок - развернуть");
            }
        };

        trayIcon.addMouseMotionListener(mouM);
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent ev) {
                if (ev.getNewState() == JFrame.ICONIFIED) {
                    setVisible(false);
                    addTray();
                }
            }
        });
    }

    // метод удаления из трея
    private void removeTray() {
        systemTray.remove(trayIcon);
    }

    // метод добавления в трей
    private void addTray() {
        try {
            systemTray.add(trayIcon);
            chetTray = true;
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        app = new TrayFrame();
        app.setVisible(true);
        app.setAlwaysOnTop(true);
        //app.setSize(777, 777);

        //обработчик основного окна - здесь необходимо перечислить все возможные действия - раз взялись обрабатывать, надо делать до конца :)
        app.addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent winEvent) {
                System.exit(0);//при закрытии окна завершаем программу
            }

            public void windowActivated(WindowEvent winEvent) {
            }

            public void windowClosed(WindowEvent winEvent) {
            }

            public void windowDeactivated(WindowEvent winEvent) {
            }

            public void windowDeiconified(WindowEvent winEvent) {
            }

            public void windowIconified(WindowEvent winEvent) {
            }

            public void windowOpened(WindowEvent winEvent) {
            }
        });
    }
}
