package cn.wanghw.utils;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

import static javax.swing.text.html.HTML.Tag.IMPLIED;
import static javax.swing.text.html.HTML.Tag.P;

public class MessageDialog extends JDialog {
    private String message;

    public MessageDialog(String message, String title) {
        this.setTitle(title);
        this.setModal(true);
        this.message = message;
        init();
    }

    public MessageDialog(JDialog dialog, String message, String title) {
        super(dialog, title, true);
        this.message = message;
        init();
    }

    public MessageDialog(Frame main, String message, String title) {
        super(main, title, true);
        this.message = message;
        init();
    }

    class CustomHTMLEditorKit extends HTMLEditorKit {
        private final ViewFactory viewFactory = new HTMLEditorKit.HTMLFactory() {
            @Override
            public View create(Element elem) {
                AttributeSet attrs = elem.getAttributes();
                Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
                Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
                if (o instanceof HTML.Tag) {
                    HTML.Tag kind = (HTML.Tag) o;
                    if (IMPLIED == kind) return new WrappableParagraphView(elem); // <pre>
                    if (P == kind) return new WrappableParagraphView(elem); // <p>
                }
                return super.create(elem);
            }
        };

        @Override
        public ViewFactory getViewFactory() {
            return this.viewFactory;
        }
    }

    class WrappableParagraphView extends javax.swing.text.html.ParagraphView {
        public WrappableParagraphView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            return View.X_AXIS == axis ? 0 : super.getMinimumSpan(axis);
        }
    }

    public void init() {
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();
        this.setResizable(true);
        this.setSize(630, 420);
        this.setLocation((d.width - this.getWidth()) / 2,
                (d.height - this.getHeight()) / 2);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final JEditorPane text = new JEditorPane() {
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        text.setEditable(false);
        text.setEditorKit(new CustomHTMLEditorKit());
        text.setText(message);
        JScrollPane scroll = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        this.getContentPane().add(panel);

        SwingUtilities.invokeLater(() -> {
            scroll.getVerticalScrollBar().setValue(0);
            text.setCaretPosition(0);
        });
        setVisible(true);
    }
}