//
//  NSObject+BNJSON.h
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSObject (BN)

- (id)asJSONObject;

- (NSString *)asJSONString;

- (id)asObjectFromJSONObject;

@end
