//
//  BNAddressesNode.m
//  BitnashKit
//
//  Created by Rich Collins on 5/2/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNKeysNode.h"
#import "BNWallet.h"
#import "BNKey.h"

@implementation BNKeysNode

- (id)init
{
    self = [super init];
    self.nodeTitle = @"Addresses";
    self.childClass = BNKey.class;
    self.nodeSuggestedWidth = 325;
    self.shouldUseCountForNodeNote = YES;
    return self;
}


- (void)fetch
{
    [self mergeWithChildren:[self.wallet keys]];
}

@end
