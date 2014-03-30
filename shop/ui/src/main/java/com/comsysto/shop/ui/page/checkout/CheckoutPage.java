package com.comsysto.shop.ui.page.checkout;

import com.comsysto.shop.service.authentication.api.FakeAuthenticationService;
import com.comsysto.shop.service.basket.api.Basket;
import com.comsysto.shop.service.checkout.api.Checkout;
import com.comsysto.shop.service.order.api.OrderService;
import com.comsysto.shop.service.order.model.DeliveryAddressInfo;
import com.comsysto.shop.service.order.model.OrderInfo;
import com.comsysto.shop.service.order.model.OrderItemInfo;
import com.comsysto.shop.service.product.model.ProductInfo;
import com.comsysto.shop.service.recommendation.api.RecommendationService;
import com.comsysto.shop.service.user.model.UserInfo;
import com.comsysto.shop.ui.event.basket.BasketChangeEvent;
import com.comsysto.shop.ui.navigation.NavigationItem;
import com.comsysto.shop.ui.page.AbstractBasePage;
import com.comsysto.shop.ui.page.home.HomePage;
import com.comsysto.shop.ui.panel.DeliveryAdressInfoPanel;
import com.comsysto.shop.ui.panel.OrderItemListPanel;
import com.comsysto.shop.ui.panel.product.RecommendationItemListPanel;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Collections;
import java.util.List;


/**
 * @author zuther
 */
@SuppressWarnings("UnusedDeclaration")
@MountPath("checkout")
@NavigationItem(name = "Proceed to Checkout", sortOrder = 3, visible = "!@basket.isEmpty()")
public class CheckoutPage extends AbstractBasePage {
    private static final long serialVersionUID = -6793984194989062010L;

    @SpringBean(name = "basket")
    private Basket basket;

    @SpringBean(name = "checkout")
    private Checkout checkout;

    @SpringBean(name = "orderService")
    private OrderService orderService;

    @SpringBean(name = "recommendationService")
    private RecommendationService recommendationService;

    @SpringBean(name = "fakeAuthenticationService")
    private FakeAuthenticationService fakeAuthenticationService;

    IModel<UserInfo> userInfoModel;
    IModel<OrderInfo> orderInfoModel;

    public CheckoutPage() {
        super();
        orderInfoModel = orderInfoModel();
        initCheckoutPage();
        validateCheckoutPage();
    }


    CheckoutPage(IModel<OrderInfo> orderInfoModel) {
        super();
        this.orderInfoModel = orderInfoModel;
        initCheckoutPage();
        validateCheckoutPage();
    }

    private void initCheckoutPage() {
        userInfoModel = userInfoModel();
        if (!getAuthenticationService().isAuthorized()) {
            fakeAuthenticationService.authenticate();
        }
        add(orderContainer());
        add(prepareFrequentlyBoughtWithPanel(checkout.getOrderItemInfos()));

    }

    protected void validateCheckoutPage() {
        if (checkout.getOrderItemInfos().isEmpty()) {
            getSession().error(getString("checkout.validation.failed"));
            throw new RestartResponseException(Application.get().getHomePage());
        }
    }

    private Component submitOrderLink() {
        return new Link<Void>("submitOrder") {
            private static final long serialVersionUID = 5203227218130238529L;

            @Override
            protected void onBeforeRender() {
                setVisible(!isReadOnly() && getAuthenticationService().isAuthorized());
                super.onBeforeRender();
            }

            @Override
            public void onClick() {
                OrderInfo submittedOrder = orderService.submitOrder(orderInfoModel.getObject(), getSession().getId());

                OrderConfirmationPage orderConfirmationPage = new OrderConfirmationPage(Model.of(submittedOrder));
                orderConfirmationPage.info(CheckoutPage.this.getString("order.submitted"));
                setResponsePage(orderConfirmationPage);
            }
        };
    }

    private IModel<UserInfo> userInfoModel() {
        return new LoadableDetachableModel<UserInfo>() {
            private static final long serialVersionUID = 2232385286290869095L;

            @Override
            protected UserInfo load() {
                return getAuthenticationService().getAuthenticatedUserInfo();
            }
        };
    }

    private IModel<OrderInfo> orderInfoModel() {
        return new LoadableDetachableModel<OrderInfo>() {
            private static final long serialVersionUID = -8140423224127500419L;

            @Override
            protected OrderInfo load() {
                if (getAuthenticationService().isAuthorized()) {
                    UserInfo authenticatedUser = userInfoModel.getObject();
                    return new OrderInfo(authenticatedUser, new DeliveryAddressInfo(authenticatedUser),
                            checkout.getOrderItemInfos(), getSession().getId());
                }
                return new OrderInfo(new UserInfo(), new DeliveryAddressInfo(), checkout.getOrderItemInfos(), "");
            }
        };
    }

    private Component orderContainer() {
        WebMarkupContainer orderContainer = new WebMarkupContainer("orderContainer") {

            @Override
            public void onEvent(IEvent<?> event) {
                if (event.getPayload() instanceof BasketChangeEvent) {
                    ((BasketChangeEvent) event.getPayload()).getTarget().add(this);
                }
            }
        };
        orderContainer.add(new DeliveryAdressInfoPanel("delieferyInfomation", orderInfoModel) {
            private static final long serialVersionUID = 8837712628875618582L;

            @Override
            public boolean isReadOnly() {
                return CheckoutPage.this.isReadOnly();
            }
        });
        orderContainer.add(new OrderItemListPanel("orderItems", orderInfoModel));
        orderContainer.add(submitOrderLink());
        orderContainer.add(backToShopLink());
        return orderContainer.setOutputMarkupId(true);
    }

    private Component backToShopLink() {
        return new BookmarkablePageLink<Void>("backToShopLink", HomePage.class) {
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                setVisible(isReadOnly());
            }
        };
    }

    protected Component prepareFrequentlyBoughtWithPanel(final List<OrderItemInfo> orderItemInfos) {
        IModel<List<ProductInfo>> productListModel = !showFrequentlyBoughtWithPanel()
                ? emptyListModel() : productListModel(orderItemInfos);
        return new RecommendationItemListPanel("frequentlyBoughtWithProducts", "FREQUENTLY_BOUGHT",
                new Model<>("Frequently bought with"), productListModel);
    }

    private IModel<List<ProductInfo>> emptyListModel() {
        return new ListModel<>(Collections.<ProductInfo>emptyList());
    }

    private LoadableDetachableModel<List<ProductInfo>> productListModel(final List<OrderItemInfo> orderItemInfos) {
        return new LoadableDetachableModel<List<ProductInfo>>() {
            @Override
            protected List<ProductInfo> load() {
                return recommendationService.getFrequentlyBoughtWithProductsRecommendations(3);
            }
        };
    }

    protected boolean isReadOnly() {
        return false;
    }

    protected boolean showFrequentlyBoughtWithPanel() {
        return true;
    }
}


