package com.comsysto.shop.ui.page;

import com.comsysto.shop.service.authentication.api.AuthenticationService;
import com.comsysto.shop.ui.application.ShopApplication;
import com.comsysto.shop.ui.event.login.HomeButtonImageRessource;
import com.comsysto.shop.ui.panel.NavigationPanel;
import com.comsysto.shop.ui.panel.base.FeedbackPanel;
import com.comsysto.shop.ui.panel.login.LoginModalPanel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;

import java.util.Collections;
import java.util.Map;

/**
 * @author zutherb
 */
public abstract class AbstractBasePage extends WebPage {

    private static final String FAVICON_HEADER = "<link rel=\"icon\" type=\"image/png\" href=\"${contextPath}/assets/ico/Icons_Map-14.png\" />";
    private static final long serialVersionUID = -9213806230323972218L;

    @SpringBean(name = "authenticationService")
    private AuthenticationService authenticationService;

    protected LoginModalPanel loginModal;
    protected WebMarkupContainer header;
    protected WebMarkupContainer navigation;
    protected Component feedback;

    public AbstractBasePage(PageParameters pageParameters) {
        super(pageParameters);
        initialize();
    }

    public AbstractBasePage(IModel<?> model) {
        super(model);
        initialize();
    }

    public AbstractBasePage() {
        super();
        initialize();
    }

    private void initialize() {
        add(loginModalPanel());
        add(headerContainer());
        add(pizzaShopFeedbackPanel());
    }

    private Component loginModalPanel() {
        loginModal = new LoginModalPanel("loginModal");
        return loginModal;
    }

    private Component headerContainer() {
        header = new WebMarkupContainer("header");
        header.add(homePageLink());
        header.add(navigation());
        return header.setOutputMarkupId(true);
    }

    private Component navigation() {
        navigation = new NavigationPanel("navigation", loginModal);
        return navigation.setOutputMarkupId(true);
    }


    private Component homePageLink() {
        BookmarkablePageLink<Void> pageLink = new BookmarkablePageLink<>("home", ShopApplication.get().getHomePage());
        pageLink.add(new Image("logo", new HomeButtonImageRessource()));
        return pageLink;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        String contextPath = RequestCycle.get().getRequest().getContextPath();

        Map<String, String> replacements = Collections.singletonMap("contextPath", getContextPath());
        MapVariableInterpolator variableInterpolator = new MapVariableInterpolator(FAVICON_HEADER, replacements);
        response.render(StringHeaderItem.forString(variableInterpolator.toString()));

        response.render(CssHeaderItem.forUrl(contextPath + "/assets/css/bootstrap.css"));
        response.render(CssHeaderItem.forUrl(contextPath + "/assets/css/bootstrap-responsive.css"));

        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-modal.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-dropdown.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-scrollspy.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-tab.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-tooltip.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-popover.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-alert.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-button.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-carousel.js"));
        response.render(JavaScriptHeaderItem.forUrl(contextPath + "/assets/js/bootstrap-typeahead.js"));

    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

     protected Component pizzaShopFeedbackPanel() {
        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        feedback.setOutputMarkupPlaceholderTag(true);
        return feedback;
    }

    protected String getContextPath() {
        return RequestCycle.get().getRequest().getContextPath();
    }
}