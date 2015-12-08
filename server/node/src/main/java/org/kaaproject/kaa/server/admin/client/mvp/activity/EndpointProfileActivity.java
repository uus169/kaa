/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.admin.client.mvp.activity;

import com.google.common.io.BaseEncoding;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import org.kaaproject.avro.ui.gwt.client.widget.BusyPopup;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEvent;
import org.kaaproject.avro.ui.gwt.client.widget.grid.event.RowActionEventHandler;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileViewDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.ClientFactory;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointGroupPlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.EndpointProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.SdkProfilePlace;
import org.kaaproject.kaa.server.admin.client.mvp.place.TopicPlace;
import org.kaaproject.kaa.server.admin.client.mvp.view.EndpointProfileView;

import java.util.ArrayList;
import java.util.List;

public class EndpointProfileActivity extends
        AbstractDetailsActivity<EndpointProfileViewDto, EndpointProfileView, EndpointProfilePlace> {

    public EndpointProfileActivity(EndpointProfilePlace place, ClientFactory clientFactory) {
        super(place, clientFactory);
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        super.start(containerWidget, eventBus);
        BusyPopup.hidePopup();
    }

    protected void bind(final EventBus eventBus) {
        super.bind(eventBus);

        registrations.add(detailsView.getGroupsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> rowActionEvent) {
                String id = rowActionEvent.getClickedId();
                EndpointGroupPlace endpointGroupPlace =
                        new EndpointGroupPlace(place.getApplicationId(), id, false, false);
                endpointGroupPlace.setPreviousPlace(place);
                goTo(endpointGroupPlace);
            }
        }));

        registrations.add(detailsView.getTopicsGrid().addRowActionHandler(new RowActionEventHandler<String>() {
            @Override
            public void onRowAction(RowActionEvent<String> rowActionEvent) {
                String id = rowActionEvent.getClickedId();
                TopicPlace topicPlace = new TopicPlace(place.getApplicationId(), id);
                topicPlace.setPreviousPlace(place);
                goTo(topicPlace);
            }
        }));
    }

    @Override
    protected String getEntityId(EndpointProfilePlace place) {
        return place.getEndpointKeyHash();
    }

    @Override
    protected EndpointProfileView getView(boolean create) {
        return clientFactory.getEndpointProfileView();
    }

    @Override
    protected EndpointProfileViewDto newEntity() {
        return null;
    }

    @Override
    protected void onEntityRetrieved() {

        detailsView.reset();

        EndpointProfileDto profileDto = entity.getEndpointProfileDto();
        EndpointUserDto userDto = entity.getEndpointUserDto();
        ProfileSchemaDto profileSchemaDto = entity.getProfileSchemaDto();

        detailsView.getKeyHash().setValue(BaseEncoding.base64().encode(profileDto.getEndpointKeyHash()));

        if (userDto != null) {
            detailsView.getUserID().setValue(userDto.getId());
            detailsView.getUserExternalID().setValue(userDto.getExternalId());

            for (Widget widget : detailsView.getUserInfoList()) {
                widget.setVisible(true);
            }
        } else {
            for (Widget widget : detailsView.getUserInfoList()) {
                widget.setVisible(false);
            }
        }

        final SdkProfileDto sdkDto = entity.getSdkProfileDto();
        if (sdkDto != null) {
            String sdkName = sdkDto.getName();
            detailsView.getSdkAnchor().setText((sdkName != null && !sdkName.isEmpty()) ? sdkName : sdkDto.getToken());
            registrations.add(detailsView.getSdkAnchor().addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    SdkProfilePlace sdkProfilePlace =
                            new SdkProfilePlace(place.getApplicationId(), sdkDto.getId());
                    sdkProfilePlace.setPreviousPlace(place);
                    goTo(sdkProfilePlace);
                }
            }));
        } else {
            detailsView.getSdkAnchor().setText("");
        }

        List<EndpointGroupDto> groupDtoList = entity.getGroupDtoList();
        if (groupDtoList != null) {
            detailsView.getGroupsGrid().getDataGrid().setRowData(groupDtoList);
        }

        List<TopicDto> endpointNotificationTopics = entity.getEndpointNotificationTopics();
        if (endpointNotificationTopics != null) {
            detailsView.getTopicsGrid().getDataGrid().setRowData(endpointNotificationTopics);
        } else {
            detailsView.getTopicsGrid().getDataGrid().setRowData(new ArrayList<TopicDto>());
        }

        detailsView.getSchemaName().setValue(profileSchemaDto.getName());
        detailsView.getDescription().setValue(profileSchemaDto.getDescription());

        RecordField endpointProfileRecord = entity.getEndpointProfileRecord();
        if (endpointProfileRecord != null) {
            detailsView.getSchemaForm().reset();
            detailsView.getSchemaForm().setValue(endpointProfileRecord);
        }
    }

    @Override
    protected void onSave() {}

    @Override
    protected void getEntity(String id, final AsyncCallback<EndpointProfileViewDto> callback) {
        KaaAdmin.getDataSource().getEndpointProfileViewDtoByEndpointProfileKeyHash(id, callback);
    }

    @Override
    protected void editEntity(EndpointProfileViewDto entity, AsyncCallback<EndpointProfileViewDto> callback) {}
}