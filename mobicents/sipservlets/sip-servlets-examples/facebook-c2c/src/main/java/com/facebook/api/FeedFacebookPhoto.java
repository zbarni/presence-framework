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

/*
  +---------------------------------------------------------------------------+
  | Facebook Development Platform Java Client                                 |
  +---------------------------------------------------------------------------+
  | Copyright (c) 2007-2008 Facebook, Inc.                                    |
  | All rights reserved.                                                      |
  |                                                                           |
  | Redistribution and use in source and binary forms, with or without        |
  | modification, are permitted provided that the following conditions        |
  | are met:                                                                  |
  |                                                                           |
  | 1. Redistributions of source code must retain the above copyright         |
  |    notice, this list of conditions and the following disclaimer.          |
  | 2. Redistributions in binary form must reproduce the above copyright      |
  |    notice, this list of conditions and the following disclaimer in the    |
  |    documentation and/or other materials provided with the distribution.   |
  |                                                                           |
  | THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR      |
  | IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES |
  | OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.   |
  | IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,          |
  | INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT  |
  | NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, |
  | DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY     |
  | THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT       |
  | (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF  |
  | THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.         |
  +---------------------------------------------------------------------------+
  | For help with this library, contact developers-help@facebook.com          |
  +---------------------------------------------------------------------------+
 */
package com.facebook.api;

import java.net.URL;

/**
 * A simple Pair consisting of a Facebook Photos photo ID for an image appearing in 
 * a newsfeed/minifeed story and the destination URL for a click
 * on that image.
 * 
 * @see IFacebookRestClient
 * @see IFacebookRestClient#photos_get
 * @see FacebookRestClient#handleFeedImages
 */
public class FeedFacebookPhoto extends Pair<Long, URL>
  implements IFeedImage {

  /**
   * Creates a linked Facebook Photos photo to appear in a user's newsfeed/minifeed.
   * 
   * @param photoId the photo ID of a Facebook photo to appear in a user's newsfeed/minifeed
   * @param link the URL to which the image should link
   * @see IFacebookRestClient#photos_get
   * @see FacebookRestClient#handleFeedImages
   */
  public FeedFacebookPhoto(Long photoId, URL link) {
    super(photoId, link);
    if (null == photoId || null == link) {
      throw new IllegalArgumentException("Both photoId and linkUrl should be provided");
    }
    if (0L >= photoId) {
      throw new IllegalArgumentException("photoId should be a Facebook Photos ID > 0");
    }
  }

  /**
   * @return the Facebook Photos photo ID of the feed image
   */
  public Long getPhotoId() {
    return getFirst();
  }

  /**
   * @return the String representation of the feed image "URL"
   */
  public String getImageUrlString() {
    return getFirst().toString();
  }

  /**
   * @return the link URL to which the feed image should link
   */
  public URL getLinkUrl() {
    return getSecond();
  }
}
