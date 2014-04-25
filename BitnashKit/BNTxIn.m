//
//  BNTxIn.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTxIn.h"

@implementation BNTxIn

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"scriptSig",
                                                   @"previousTxSerializedHex",
                                                   @"previousTxHash",
                                                   @"previousOutIndex",
                                                   nil]];
    return self;
}

@end
