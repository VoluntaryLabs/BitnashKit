//
//  BNDepositKey.m
//  BitnashKit
//
//  Created by Rich Collins on 5/20/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNDepositKey.h"
#import "BNWallet.h"

@implementation BNDepositKey

- (void)fetch
{
    [self copySlotsFrom:[(BNWallet *)self.bnParent depositKey]];
    [self postSelfChanged];
    [self postParentChainChanged];
}

- (NSString *)nodeTitle
{
    return @"Deposit Address";
}

- (NSString *)nodeSubtitle
{
    return nil;
}

@end
