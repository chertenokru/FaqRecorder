package ru.chertenok.telegraph;

import org.telegram.telegraph.ExecutorOptions;
import org.telegram.telegraph.TelegraphContext;
import org.telegram.telegraph.TelegraphContextInitializer;
import org.telegram.telegraph.api.methods.CreateAccount;
import org.telegram.telegraph.api.methods.CreatePage;
import org.telegram.telegraph.api.objects.*;
import org.telegram.telegraph.exceptions.TelegraphException;
import ru.chertenok.telegrambot.faqrecorder.config.Config;
import ru.chertenok.telegrambot.faqrecorder.dao.FaqDbDao;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegraphPublisher implements TextPublisher {
    private String autor;
    private String token;
    private FaqDbDao faqDbDao;

    public TelegraphPublisher(FaqDbDao faqDbDao) {
        // Initialize context
        TelegraphContextInitializer.init();
        TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());


        autor = faqDbDao.getAuthor();
        token = faqDbDao.getToken();
        if (autor == null) {
            // Create account
            try {
                Account account = new CreateAccount(Config.TELEGRAPH_AUTHOR_ACC)
                        .setAuthorName(Config.TELEGRAPH_AUTHOR)
                        .execute();
                faqDbDao.addAuthor(account.getShortName(), account.getAuthorName(), account.getAccessToken());
                autor = account.getAuthorName();
                token = account.getAccessToken();
            } catch (TelegraphException e) {
                e.printStackTrace();
                new Exception("Не удалось зарегистрироваться в Телеграф");
            }

        }
    }

    @Override
    public String getUrl(@NotNull List<String> text, @NotNull String title, String author) {
        Page page = null;
        try {
            page = new CreatePage(token, title, parseStringList(text))
                    .setAuthorName((author != null) ? autor : this.autor)
                    .setReturnContent(true)
                    .execute();
            return page.getUrl();
        } catch (TelegraphException e) {
            e.printStackTrace();
        }


        return null;
    }

    private List<Node> parseStringList(List<String> inText) {
        //[ {"tag":"p","children":["A link to Stackoverflow ",{"tag":"a","attrs":{"href":"http://stackoverflow.com/","target":"_blank"},"children":["http://stackoverflow.com"]}]} ],


        List<Node> newList = new ArrayList<>();


        for (String str : inText) {
            // paragraph
            NodeElement ne_p = new NodeElement();
            ne_p.setTag("p");
            List<Node> nd_p = new ArrayList<>();
            nd_p.add(new NodeText(" "));
            ne_p.setChildren(nd_p);
            newList.add(ne_p);

            str = str.replaceAll("\n", " \n ");
            String[] s = str.split(" ");
            for (String s1 : s) {

                NodeElement ne = new NodeElement();
                Map m = new HashMap<String, String>();
                if ((s1.startsWith("http")) || (s1.startsWith("www"))) {
                    ne.setTag("a");
                    m.put("href", s1);
                    m.put("target", "_blank");
                    ne.setAttrs(m);
                    List<Node> nd = new ArrayList<>();
                    nd.add(new NodeText(s1));
                    ne.setChildren(nd);
                    newList.add(ne);
                } else {
                    ne.setTag("em");
                    List<Node> nd = new ArrayList<>();
                    nd.add(new NodeText(s1 + " "));
                    ne.setChildren(nd);

                    newList.add(ne);
                }
            }

        }

        System.out.println(newList.toString());


        return newList;
    }


    /*
     TelegraphLogger.setLevel(Level.ALL);
        TelegraphLogger.registerLogger(new ConsoleHandler());

        // Initialize context
        TelegraphContextInitializer.init();
        TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());

        try {
            // Create account
            Account account = new CreateAccount("TestTelegraphApi")
                    .setAuthorName("TelegraphApi")
                    .execute();

            // Edit account
            Account editedAccount = new EditAccountInfo(account.getAccessToken())
                    .setAuthorName("Default user")
                    .setShortName("Short name")
                    .execute();

            // Get account info
            editedAccount = new GetAccountInfo(account.getAccessToken())
                    .execute();

            Node contentNode= new NodeText("My content");
            List<Node> content = new ArrayList<>();
            content.add(contentNode);

            // Create new account
            Page page = new CreatePage(account.getAccessToken(), "My title", content)
                    .setAuthorName("Random author")
                    .setReturnContent(true)
                    .execute();

            // Get page
            page = new GetPage(page.getPath()).setReturnContent(true).execute();

            Node tagNode = new NodeElement("p", new HashMap<>(), content);
            List<Node> tagContent = new ArrayList<>();
            tagContent.add(tagNode);

            // Edit page
            Page editedPage = new EditPage(account.getAccessToken(), page.getPath(), page.getTitle(), tagContent)
                    .setAuthorName("New Author")
                    .execute();

            // Get page list
            PageList pageList = new GetPageList(account.getAccessToken())
                    .setLimit(10)
                    .execute();

            // Get page view
            PageViews views = new GetViews(page.getPath())
                    .setYear(2016)
                    .execute();

            // Revoke account token
            Account revokedAccount = new RevokeAccessToken(account.getAccessToken()).execute();
        } catch (TelegraphException e) {
            TelegraphLogger.severe("MAIN", e);
        }
    }
     */

}
