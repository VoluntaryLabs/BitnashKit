//
//  BNKey.h
//  BitnashKit
//
//  Created by Rich Collins on 5/1/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNObject.h"

@interface BNKey : BNObject

@property NSString *pubKey;
@property NSString *address;
@property NSNumber *creationTime;

- (NSDate *)creationDate;

@end
