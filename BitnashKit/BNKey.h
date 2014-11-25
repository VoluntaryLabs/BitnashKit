//
//  BNKey.h
//  BitnashKit
//
//  Created by Rich Collins on 5/1/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import "BNObject.h"

@interface BNKey : BNObject

@property (strong, nonatomic) NSString *pubKey;
@property (strong, nonatomic) NSString *address;
@property (strong, nonatomic) NSNumber *creationTime;

- (NSDate *)creationDate;

@end
