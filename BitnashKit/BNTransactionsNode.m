//
//  BNTransactions.m
//  BitnashKit
//
//  Created by Rich Collins on 5/2/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNTransactionsNode.h"
#import "BNWallet.h"

@implementation BNTransactionsNode

- (id)init
{
    self = [super init];
    
    self.nodeTitle = @"Transactions";
    self.nodeSuggestedWidth = 480;
    self.shouldUseCountForNodeNote = YES;
    
    return self;
}

- (void)fetch
{
    [self mergeWithChildren:[self.wallet transactions]];
}

@end
