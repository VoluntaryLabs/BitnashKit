//
//  BNPayToAddressScriptPubKey.m
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNPayToAddressScriptPubKey.h"

@implementation BNPayToAddressScriptPubKey

+ (NSArray *)jsonProperties
{
    return [NSArray arrayWithObjects:@"address", nil];
}

@end
