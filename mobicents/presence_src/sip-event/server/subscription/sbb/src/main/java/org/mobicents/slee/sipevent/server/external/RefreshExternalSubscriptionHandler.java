/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.sipevent.server.external;

import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.facilities.Tracer;

import org.mobicents.slee.sipevent.server.subscription.ImplementedSubscriptionControlSbbLocalObject;
import org.mobicents.slee.sipevent.server.subscription.SubscriptionControlSbb;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription;
import org.mobicents.slee.sipevent.server.subscription.data.Subscription.Status;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionControlDataSource;
import org.mobicents.slee.sipevent.server.subscription.data.SubscriptionKey;

/**
 * Handles the removal of a SIP subscription
 * 
 * @author martins
 * 
 */
public class RefreshExternalSubscriptionHandler {

	private static Tracer tracer;
	private ExternalSubscriptionHandler externalSubscriptionHandler;

	public RefreshExternalSubscriptionHandler(ExternalSubscriptionHandler h) {
		externalSubscriptionHandler = h;
		if (tracer == null) {
			tracer = externalSubscriptionHandler.sbb.getSbbContext().getTracer(getClass().getSimpleName());
		}
	}
	
	public void removeExternalSubscription(String subscriber, String notifier,
			String eventPackage, String subscriptionId,
			SubscriptionControlDataSource dataSource,
			ImplementedSubscriptionControlSbbLocalObject childSbb, Subscription subscription) {
//
//		SubscriptionControlSbb sbb = externalSubscriptionHandler.sbb;
//
//
//		if (subscription == null) {
//			// subscription does not exists
//			sbb.getParentSbb().unsubscribeError(subscriber, notifier,
//					eventPackage, subscriptionId,
//					Response.CONDITIONAL_REQUEST_FAILED);
//			return;
//		}
//
//		ActivityContextInterface aci = sbb.getActivityContextNamingfacility()
//				.lookup(subscriptionKey.toString());
//		if (aci == null) {
//			tracer
//					.severe("Failed to retrieve aci for internal subscription with key "
//							+ subscriptionKey);
//			sbb.getParentSbb().unsubscribeError(subscriber, notifier,
//					eventPackage, subscriptionId,
//					Response.SERVER_INTERNAL_ERROR);
//			return;
//		}
//
//		// send OK response
//		sbb.getParentSbb().unsubscribeOk(subscriber, notifier, eventPackage,
//				subscriptionId);
//
//		if (subscription.isResourceList()) {
//			internalSubscriptionHandler.sbb.getEventListSubscriptionHandler().removeSubscription(subscription);
//		}
//		
//		removeInternalSubscription(aci, subscription, dataSource, childSbb);
		
	}

//	public void removeInternalSubscription(ActivityContextInterface aci,
//			Subscription subscription, SubscriptionControlDataSource dataSource,
//			ImplementedSubscriptionControlSbbLocalObject childSbb) {
//
//		// cancel timer
//		internalSubscriptionHandler.sbb.getTimerFacility().cancelTimer(
//				subscription.getTimerID());
//
//		if (subscription.getStatus() != Status.terminated && subscription.getStatus() != Status.waiting) {
//			// change subscription state
//			subscription.setStatus(Subscription.Status.terminated);
//			subscription.setLastEvent(null);
//		}
//
//		// check resulting subscription state
//		if (subscription.getStatus() == Subscription.Status.terminated) {
//			if (tracer.isInfoEnabled()) {
//				tracer.info("Status changed for " + subscription);
//			}
//			// remove subscription data
//			internalSubscriptionHandler.sbb.removeSubscriptionData(
//					dataSource, subscription, null, aci, childSbb);
//		} else if (subscription.getStatus() == Subscription.Status.waiting) {
//			if (tracer.isInfoEnabled()) {
//				tracer.info("Status changed for " + subscription);
//			}
//			// keep the subscription for default waiting time so notifier may
//			// know about this attemp to subscribe him
//			// refresh subscription
//			int defaultWaitingExpires = internalSubscriptionHandler.sbb
//					.getConfiguration().getDefaultWaitingExpires();
//			subscription.refresh(defaultWaitingExpires);
//			// set waiting timer
//			internalSubscriptionHandler.sbb
//					.setSubscriptionTimerAndPersistSubscription(subscription, defaultWaitingExpires + 1, aci);
//		}
//
//		// notify winfo subscription(s)
//		internalSubscriptionHandler.sbb
//				.getWInfoSubscriptionHandler()
//				.notifyWinfoSubscriptions(dataSource, subscription, childSbb);
//
//		// notify subscriber
//		internalSubscriptionHandler.getInternalSubscriberNotificationHandler()
//		.notifyInternalSubscriber( subscription, aci,
//				childSbb);
//
//	}
}
