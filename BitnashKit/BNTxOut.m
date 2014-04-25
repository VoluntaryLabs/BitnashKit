//
//  BNTxOut.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTxOut.h"

@implementation BNTxOut

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"value",
                                                   @"scriptPubKey",
                                                   nil]];
    return self;
}

@end
