//
//  BNTxIn.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTxIn.h"

@implementation BNTxIn

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:@"scriptSig", @"previousTxSerializedHex", @"previousTxHash", @"previousOutIndex", nil];
}

@end
