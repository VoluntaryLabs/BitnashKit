//
//  BNPayToAddressScriptSig.m
//  BitnashKit
//
//  Created by Rich Collins on 3/25/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNPayToAddressScriptSig.h"

@implementation BNPayToAddressScriptSig

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"pubKey",
                                                   @"signature",
                                                   nil]];
    return self;
}

@end
